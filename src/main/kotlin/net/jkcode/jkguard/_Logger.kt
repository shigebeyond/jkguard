package net.jkcode.jkguard

import net.jkcode.jkutil.common.ModuleLogSwitcher
import org.slf4j.LoggerFactory

// 守护者的日志
internal val switcher = ModuleLogSwitcher("guard")
val guardLogger = switcher.getLogger("net.jkcode.jkguard")