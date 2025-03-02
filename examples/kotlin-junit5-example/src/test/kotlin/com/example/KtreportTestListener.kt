package com.example

/**
 * KtreportTestListener - A JUnit Platform TestExecutionListener that captures and serializes test results.
 *
 * This listener captures detailed information about test execution including:
 * - Test status (passed, failed, skipped)
 * - Execution time and duration
 * - Standard output and error streams
 * - Exception details for failed tests
 *
 * The results are serialized to JSON and written to a file for further analysis or reporting.
 */

import com.example.SpecId.Companion.TYPES_EXCLUDED
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.support.descriptor.*
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull
import org.junit.platform.engine.UniqueId as UniqueIdJUnit

/**
 * Serializer for UniqueId objects.
 * Handles conversion between UniqueId objects and their string representation for JSON serialization.
 */
object UniqueIdSerializer : KSerializer<UniqueId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UniqueId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UniqueId) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): UniqueId {
        throw UnsupportedOperationException()
    }
}

/**
 * Serializer for SpecId objects.
 * Handles conversion between SpecId objects and their string representation for JSON serialization.
 */
object SpecIdSpecSerializer : KSerializer<SpecId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UniqueId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SpecId) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): SpecId {
        throw UnsupportedOperationException()
    }
}

/**
 * Serializer for TestExecutionResult.Status enum values.
 * Converts status values to their string names for JSON serialization.
 */
object StatusSerializer : KSerializer<Status> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Status", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Status) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): Status {
        throw UnsupportedOperationException()
    }
}

/**
 * Serializer for Instant objects.
 * Converts Instant objects to epoch milliseconds for JSON serialization.
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilli())
    }

    override fun deserialize(decoder: Decoder): Instant {
        throw UnsupportedOperationException()
    }
}

/**
 * Serializer for TestIdentifier objects.
 * Converts TestIdentifier objects to TestIdentifierInfo for JSON serialization.
 */
object TestIdentifierSerializer : KSerializer<TestIdentifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TestIdentifier", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TestIdentifier) {
        val info = TestIdentifierInfo.from(value)
        encoder.encodeSerializableValue(TestIdentifierInfo.serializer(), info)
    }

    override fun deserialize(decoder: Decoder): TestIdentifier {
        throw UnsupportedOperationException()
    }
}

/**
 * Data class representing test source information.
 * Extracts and normalizes information from various TestSource implementations.
 */
@Serializable
data class TestSourceInfo(
    val name: String,
    val full: String,
) {
    companion object {
        fun from(testSource: TestSource): TestSourceInfo? {
            return when (testSource) {
                is UriSource -> TestSourceInfo(testSource.uri.toString(), testSource.toString())
                is ClassSource -> TestSourceInfo(testSource.className, testSource.toString())
                is MethodSource -> TestSourceInfo(testSource.methodName, testSource.toString())
                is PackageSource -> TestSourceInfo(testSource.packageName, testSource.toString())
                is CompositeTestSource -> TestSourceInfo(
                    "Unknown",
                    "${testSource.sources.size} sources composite"
                )

                else -> TestSourceInfo("Unknown", testSource.toString())
            }
        }
    }
}

/**
 * Data class representing essential information from a TestIdentifier.
 * Extracts display name, type, tags, and source information for serialization.
 */
@Serializable
data class TestIdentifierInfo(
    val displayName: String,
    val type: String,
    val tags: Set<String>,
    val testSourceName: String? = null,
    val testSourceFull: String? = null,
) {
    companion object {
        fun from(testIdentifier: TestIdentifier): TestIdentifierInfo {
            val testSource = testIdentifier.source.getOrNull()?.let { TestSourceInfo.from(it) }
            return TestIdentifierInfo(
                displayName = testIdentifier.displayName,
                type = testIdentifier.type.name,
                tags = testIdentifier.tags.map { it.name }.toSet(),
                testSourceName = testSource?.name,
                testSourceFull = testSource?.full,
            )
        }
    }
}

/**
 * Represents a unique identifier for a test.
 * Simplified version of JUnit's UniqueId that excludes engine segments.
 */
data class UniqueId(val value: String) {
    companion object {
        fun from(uniqueId: UniqueIdJUnit): UniqueId {
            return UniqueId(
                uniqueId.segments.filter { it.type.lowercase() != "engine" }.joinToString("/") { it.value }
            )
        }
    }
}

/**
 * Represents a specification identifier for grouping related tests.
 * Filters out common test-related segments to focus on the specification structure.
 */
data class SpecId(val value: String) {
    companion object {
        private val TYPES_EXCLUDED = setOf("test", "engine", "method", "test-template", "test-template-invocation")

        fun from(uniqueId: UniqueIdJUnit): SpecId {
            return SpecId(
                uniqueId.segments.filter { !TYPES_EXCLUDED.contains(it.type.lowercase()) }.joinToString("/") { it.value }
            )
        }
    }
}

/**
 * Sealed class hierarchy representing the state of a test execution.
 * Tests transition from Running to Completed state during execution.
 */
@Serializable
sealed class TestResult {
    @Serializable(with = SpecIdSpecSerializer::class)
    abstract val specId: SpecId

    @Serializable(with = UniqueIdSerializer::class)
    abstract val uniqueId: UniqueId

    @Serializable(with = TestIdentifierSerializer::class)
    abstract val identifier: TestIdentifier

    /**
     * Represents a test that is currently running.
     * Contains the test identifier and start time information.
     */
    @Serializable
    data class Running(
        @Serializable(with = SpecIdSpecSerializer::class)
        override val specId: SpecId,
        @Serializable(with = UniqueIdSerializer::class)
        override val uniqueId: UniqueId,
        @Serializable(with = TestIdentifierSerializer::class)
        override val identifier: TestIdentifier,
        @Serializable(with = InstantSerializer::class)
        val startTime: Instant
    ) : TestResult() {
        /**
         * Creates a Completed result from this Running result.
         * 
         * @param status The final execution status
         * @param endTime The time when the test completed
         * @param stdout Captured standard output
         * @param stderr Captured standard error
         * @param throwable Any exception that occurred during test execution
         * @return A Completed test result
         */
        fun complete(status: Status, endTime: Instant, stdout: String, stderr: String, throwable: Throwable?) =
            Completed(
                specId = specId,
                uniqueId = uniqueId,
                identifier = identifier,
                status = status,
                startTime = startTime,
                durationMs = Duration.between(startTime, endTime).toMillis(),
                stdout = stdout,
                stderr = stderr,
                throwable = throwable?.let {
                    val exceptionOutput = ByteArrayOutputStream()
                    it.printStackTrace(PrintStream(exceptionOutput))
                    exceptionOutput.toString()
                }
            )
    }

    /**
     * Represents a test that has completed execution.
     * Contains all execution details including status, duration, and output.
     */
    @Serializable
    data class Completed(
        @Serializable(with = SpecIdSpecSerializer::class)
        override val specId: SpecId,
        @Serializable(with = UniqueIdSerializer::class)
        override val uniqueId: UniqueId,
        @Serializable(with = TestIdentifierSerializer::class)
        override val identifier: TestIdentifier,
        @Serializable(with = StatusSerializer::class)
        val status: Status,
        @Serializable(with = InstantSerializer::class)
        val startTime: Instant,
        val durationMs: Long,
        val stdout: String,
        val stderr: String,
        val throwable: String?
    ) : TestResult()
}

/**
 * Data class representing the results of an entire test suite execution.
 * Contains aggregated statistics and all individual test results.
 */
@Serializable
data class TestSuiteResult(
    val testResults: List<TestResult.Completed>,
    val totalTests: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    @Serializable(with = InstantSerializer::class)
    val startTime: Instant,
    @Serializable(with = InstantSerializer::class)
    val endTime: Instant,
    val totalDurationMs: Long
)

/**
 * JUnit Platform TestExecutionListener implementation that captures detailed test results.
 * 
 * This listener:
 * 1. Captures the start and end time of the test suite
 * 2. Records individual test executions with their status
 * 3. Captures standard output and error streams for each test
 * 4. Captures exception details for failed tests
 * 5. Serializes all results to a JSON file after test execution completes
 *
 * The JSON output can be used for custom reporting, analysis, or integration with other tools.
 */
class KtreportTestListener : TestExecutionListener {
    private val testResults = ConcurrentHashMap<UniqueIdJUnit, TestResult>()
    private val testOutputs = ThreadLocal<Pair<ByteArrayOutputStream, ByteArrayOutputStream>>()
    private var suiteStartTime: Instant = Instant.now()
    private var suiteEndTime: Instant = Instant.now()

    companion object {
        private const val TEST_RESULTS_FILE_NAME = "build/test-results/ktreport.json"
        private val ORIGINAL_OUT = System.out
        private val ORIGINAL_ERR = System.err
    }

    /**
     * Called when the test plan execution starts.
     * Records the start time of the test suite.
     *
     * @param testPlan The test plan being executed
     */
    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        suiteStartTime = Instant.now()
    }

    /**
     * Called when the test plan execution finishes.
     * Aggregates all test results, creates a TestSuiteResult, and writes it to a JSON file.
     *
     * @param testPlan The test plan that was executed
     */
    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        suiteEndTime = Instant.now()

        val completedResults = testResults.values
            .filterIsInstance<TestResult.Completed>()
            .toList()

        val suiteResult = TestSuiteResult(
            testResults = completedResults.sortedBy { it.uniqueId.toString() },
            totalTests = completedResults.size,
            passed = completedResults.count { it.status == Status.SUCCESSFUL },
            failed = completedResults.count { it.status == Status.FAILED },
            skipped = completedResults.count { it.status == Status.ABORTED },
            startTime = suiteStartTime,
            endTime = suiteEndTime,
            totalDurationMs = Duration.between(suiteStartTime, suiteEndTime).toMillis()
        )

        val json = Json { prettyPrint = true }
        val resultJson = json.encodeToString(suiteResult)

        val outputFile = File(TEST_RESULTS_FILE_NAME)
        if (!outputFile.parentFile.exists()) {
            if (!outputFile.parentFile.mkdirs()) {
                throw Exception("Failed to create parent directory.")
            }
        }
        outputFile.writeText(resultJson)

        println("[ktreport] Test results have been written to: ${File("test-results.json").absolutePath}")
    }

    /**
     * Called when a test starts execution.
     * Creates a Running test result and sets up output capture for the test.
     *
     * @param testIdentifier The identifier of the test that is starting
     */
    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (testIdentifier.isTest) {
            val uniqueId = testIdentifier.uniqueIdObject

            // Record test start state
            testResults[uniqueId] = TestResult.Running(
                specId = SpecId.from(uniqueId),
                uniqueId = UniqueId.from(uniqueId),
                identifier = testIdentifier,
                startTime = Instant.now()
            )

            // Capture standard output and standard error output
            val stdoutCapture = ByteArrayOutputStream()
            val stderrCapture = ByteArrayOutputStream()
            testOutputs.set(Pair(stdoutCapture, stderrCapture))

            System.setOut(PrintStream(stdoutCapture))
            System.setErr(PrintStream(stderrCapture))
        }
    }

    /**
     * Called when a test finishes execution.
     * Completes the test result with status, duration, and captured output.
     *
     * @param testIdentifier The identifier of the test that finished
     * @param testExecutionResult The result of the test execution
     */
    override fun executionFinished(
        testIdentifier: TestIdentifier,
        testExecutionResult: TestExecutionResult
    ) {
        if (testIdentifier.isTest) {
            val endTime = Instant.now()

            // Get current state
            val testId = testIdentifier.uniqueIdObject
            val currentState = testResults[testId]
            if (currentState !is TestResult.Running) {
                throw IllegalStateException("Unexpected state: $currentState")
            }

            // Restore standard output and standard error output
            System.setOut(ORIGINAL_OUT)
            System.setErr(ORIGINAL_ERR)

            // Get captured output
            val (stdoutCapture, stderrCapture) = testOutputs.get()

            // Save test result
            testResults[testId] = currentState.complete(
                status = testExecutionResult.status,
                endTime = endTime,
                stdout = stdoutCapture.toString(),
                stderr = stderrCapture.toString(),
                throwable = testExecutionResult.throwable.getOrNull(),
            )
        }
    }
}
