package fyi.lehlanyaverse.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.core.Page
import fyi.lehlanyaverse.components.fx.SectorTracker
import fyi.lehlanyaverse.components.layout.SITE_NAME
import fyi.lehlanyaverse.components.layout.PersonJsonLd
import fyi.lehlanyaverse.components.layout.Seo
import fyi.lehlanyaverse.components.sections.ActiveOperations
import fyi.lehlanyaverse.components.sections.Comms
import fyi.lehlanyaverse.components.sections.Commendations
import fyi.lehlanyaverse.components.sections.FieldIntel
import fyi.lehlanyaverse.components.sections.Loadout
import fyi.lehlanyaverse.components.sections.OperatorProfile
import fyi.lehlanyaverse.components.sections.SectorBreak
import fyi.lehlanyaverse.components.sections.ServiceRecord
import fyi.lehlanyaverse.data.Intel

/** The dossier: all seven sections in reading order. */
@Page
@Composable
fun HomePage() {
    val op = Intel.operator
    Seo(
        title = "${op.name}: ${op.role}, ${op.specialty} // $SITE_NAME",
        description = "Mission dossier of ${op.name}, a ${op.specialty.lowercase()} software engineer in ${op.location}: " +
            "loadout, service record, commendations and active operations.",
        path = "/",
    )
    PersonJsonLd()
    OperatorProfile()
    SectorBreak(1)
    Loadout()
    SectorBreak(2)
    ServiceRecord()
    SectorBreak(3)
    Commendations()
    SectorBreak(4)
    ActiveOperations()
    SectorBreak(5)
    FieldIntel()
    SectorBreak(6)
    Comms()
    SectorTracker()
}
