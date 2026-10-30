package fyi.lehlanyaverse

import fyi.lehlanyaverse.components.sections.BOARD_COLS
import fyi.lehlanyaverse.components.sections.layoutBoard
import fyi.lehlanyaverse.components.sections.links
import fyi.lehlanyaverse.data.BoardSpot
import fyi.lehlanyaverse.data.Intel
import fyi.lehlanyaverse.data.IntelPaper
import fyi.lehlanyaverse.data.IntelType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Guards for the evidence board's layout (BoardLayout.kt). Run with `./gradlew :site:jsNodeTest`. */
class BoardLayoutTest {
    private fun paper(id: String, linkedTo: List<String> = emptyList(), at: BoardSpot? = null) =
        IntelPaper(id, id, IntelType.NOTE, "t", "s", emptyList(), linkedTo = linkedTo, position = at)

    @Test
    fun aLinkBothWaysIsOneWire() {
        val papers = listOf(paper("a", listOf("b")), paper("b", listOf("a", "c", "missing")), paper("c"))
        assertEquals(listOf("a" to "b", "b" to "c"), links(papers))
        assertEquals(2, layoutBoard(papers).wires.size)
    }

    @Test
    fun positionedPapersGoExactlyWhereSet() {
        val p = layoutBoard(listOf(paper("a", at = BoardSpot(12.0, 34.0)))).papers.single()
        assertEquals(12.0 to 34.0, p.x to p.y)
    }

    @Test
    fun autoPlacedPapersTakeFreeCells() {
        val papers = listOf(paper("a", at = BoardSpot(10.0, 10.0))) + (1..8).map { paper("p$it") }
        val layout = layoutBoard(papers)
        val cells = layout.papers.map { cellOf(it.x, it.y, layout.rows) }
        assertEquals(cells.size, cells.toSet().size, "Two papers share a cell: $cells")
    }

    @Test
    fun autoPlacedPapersLandNearTheirLinks() {
        val papers = listOf(paper("far", at = BoardSpot(88.0, 60.0)), paper("x", linkedTo = listOf("far")))
        val x = layoutBoard(papers).papers.first { it.paper.id == "x" }
        assertTrue(x.x > 50, "'x' should sit near 'far' on the right, was at ${x.x}%")
    }

    @Test
    fun realBoardLaysOutEveryPaperOnTheBoard() {
        val layout = layoutBoard(Intel.fieldIntel)
        assertEquals(Intel.fieldIntel.size, layout.papers.size, "A paper has no spot")
        layout.papers.forEach { assertTrue(it.x in 0.0..100.0 && it.y in 0.0..100.0, "${it.paper.id} is off the board") }
        assertEquals(links(Intel.fieldIntel).size, layout.wires.size, "A link has no string")
    }

    private fun cellOf(x: Double, y: Double, rows: Int) =
        (y / (100.0 / rows)).toInt() * BOARD_COLS + (x / (100.0 / BOARD_COLS)).toInt()
}
