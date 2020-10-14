package com.adarshr.gradle.testlogger

import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.testing.logging.TestLogging
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import static org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT

class TestLoggerExtensionSpec extends Specification {

    def "test logger extension default properties"() {
        when:
            def extension = new TestLoggerExtension()
        then:
            extension.theme == ThemeType.STANDARD
            extension.showExceptions
            extension.slowThreshold == 2000
            extension.showSummary
            !extension.showStandardStreams
            extension.showPassedStandardStreams
            extension.showSkippedStandardStreams
            extension.showFailedStandardStreams
            extension.showPassed
            extension.showSkipped
            extension.showFailed
    }

    def "test logger extension properties can be overridden"() {
        given:
            def overrides = [
                theme: 'plain',
                showExceptions: 'false',
                slowThreshold: '4000',
                showSummary: 'false',
                showStandardStreams: 'true',
                showPassedStandardStreams: 'false',
                showSkippedStandardStreams: 'false',
                showFailedStandardStreams: 'false',
                showPassed: 'false',
                showSkipped: 'false',
                showFailed: 'false',
                logLevel: 'warn'
            ]
        when:
            def extension = new TestLoggerExtension()
            extension.applyOverrides(overrides)
        then:
            extension.theme == ThemeType.PLAIN
            !extension.showExceptions
            extension.slowThreshold == 4000
            !extension.showSummary
            extension.showStandardStreams
            !extension.showPassedStandardStreams
            !extension.showSkippedStandardStreams
            !extension.showFailedStandardStreams
            !extension.showPassed
            !extension.showSkipped
            !extension.showFailed
            extension.logLevel == LogLevel.WARN
    }

    def "combine two test extension objects"() {
        given:
            def parent = new TestLoggerExtension()
            parent.theme = ThemeType.MOCHA
            parent.showPassed = false
            parent.showSkipped = true
            parent.slowThreshold = 10000
        and:
            def child = new TestLoggerExtension()
            child.theme = ThemeType.MOCHA_PARALLEL
            child.slowThreshold = 20000
            child.showSkipped = false
        when:
            def combined = child.combine(parent)
        then:
            combined.theme == ThemeType.MOCHA_PARALLEL
            combined.slowThreshold == 20000
            !combined.showPassed
            !combined.showSkipped
    }

    @Unroll
    def "test logger extension reacts to testLogging properties #testLoggingProperties"() {
        given:
            def testLoggingMock = Mock(TestLogging)
            testLoggingProperties.each { property, value ->
                testLoggingMock."${property}" >> value
            }
            def extension = new TestLoggerExtension()
        when:
            def reacted = extension.reactTo(testLoggingMock)
        then:
            extensionProperties.each { property, value ->
                assert reacted."${property}" == value
            }
        where:
            testLoggingProperties                           | extensionProperties
            [showStandardStreams: true]                     | [showStandardStreams: true]
            [showStandardStreams: false]                    | [showStandardStreams: false]
            [showExceptions: true]                          | [showExceptions: true]
            [showExceptions: false]                         | [showExceptions: false]
            [showCauses: true]                              | [showCauses: true]
            [showCauses: false]                             | [showCauses: false]
            [showStackTraces: true, exceptionFormat: SHORT] | [showStackTraces: true, showFullStackTraces: false]
            [showStackTraces: false]                        | [showStackTraces: false, showFullStackTraces: false]
            [showStackTraces: true, exceptionFormat: FULL]  | [showStackTraces: true, showFullStackTraces: true]
    }

    @Unroll
    def "test logger extension does not react to testLogging if extension properties #extensionProperties have been configured"() {
        given:
            def testLoggingMock = Mock(TestLogging)
            testLoggingProperties.each { property, value ->
                testLoggingMock."${property}" >> value
            }
            def extension = new TestLoggerExtension()
        and:
            extensionProperties.each { property, value ->
                extension."${property}" = value
            }
        when:
            def reacted = extension.reactTo(testLoggingMock)
        then:
            extensionProperties.each { property, value ->
                assert reacted."${property}" == value
            }
        where:
            testLoggingProperties                           | extensionProperties
            [showStandardStreams: true]                     | [showStandardStreams: false]
            [showExceptions: true]                          | [showExceptions: false]
            [showCauses: true]                              | [showCauses: false]
            [showStackTraces: true, exceptionFormat: SHORT] | [showStackTraces: false, showFullStackTraces: false]
            [showStackTraces: true, exceptionFormat: FULL]  | [showStackTraces: false, showFullStackTraces: false]
    }
}
