package net.jkcode.jkguard

import net.jkcode.jkutil.common.JkException

/**
 * 守护异常
 */
class GuardException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}