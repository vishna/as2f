@file:JvmName("Main")
package cli

import dev.vishna.patrol.*
import dev.vishna.as2f.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

fun main(args: CommandArgs) = args.patrol {

    val inspectionJobs = ConcurrentHashMap<String, Job>()

    name {
        "as2f"
    }

    help {
        "Parses Android resource strings and generates i18n dart code"
    }

    onInspection { scope, watchPoint, dryRun, runOnce ->
        scope.launch {
            generateCode(
                name = watchPoint.name,
                source = watchPoint.source,
                target = requireNotNull(watchPoint["target"] as String?) { "target value not provided in $watchPoint" },
                dryRun = dryRun
            )
        }.apply {
            inspectionJobs[watchPoint.name]?.cancel()
            inspectionJobs[watchPoint.name] = this
            if (runOnce) {
                runBlocking {
                    join()
                }
            }
        }
    }

    bootstrap(::bootstrapAs2fPatrolConfig)
}