package fyi.lehlanyaverse.components.sections

import fyi.lehlanyaverse.data.IntelPaper
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/** Columns of the auto-placement grid. A paper is about a quarter of the board wide. */
internal const val BOARD_COLS = 4

/** Tilts for papers that don't set one, cycled by index. */
private val TILTS = listOf(-3.0, 2.0, -1.0, 3.0, -2.0)

/** A paper on the board: its pin at ([x], [y]) in % of the board, turned [rot] degrees round it. */
internal data class Placed(val paper: IntelPaper, val index: Int, val x: Double, val y: Double, val rot: Double)

/** Red string between two papers ([a] < [b] by id), as an SVG path in the board's 0..100 box. */
internal data class Wire(val a: String, val b: String, val d: String)

internal data class BoardLayout(val papers: List<Placed>, val wires: List<Wire>, val rows: Int)

/**
 * Lays out the evidence board, all in % so it never needs measuring: it renders the same in the export snapshot
 * and stays pinned to the pins at any width.
 *
 * Papers with a `position` go exactly there. The rest take free cells of a [BOARD_COLS] × rows grid (a cell is
 * taken when a pin falls in it), each the free cell nearest the papers it links to, else the first in reading order.
 */
internal fun layoutBoard(papers: List<IntelPaper>): BoardLayout {
    val rows = max(2, ceil(papers.size / BOARD_COLS.toDouble()).toInt())
    fun cellOf(x: Double, y: Double) =
        (y / (100.0 / rows)).toInt().coerceIn(0, rows - 1) * BOARD_COLS + (x / (100.0 / BOARD_COLS)).toInt().coerceIn(0, BOARD_COLS - 1)

    val spots = mutableMapOf<String, Pair<Double, Double>>()
    papers.forEach { p -> p.position?.let { spots[p.id] = it.x to it.y } }
    val free = (0 until rows * BOARD_COLS).toMutableList()
    spots.values.forEach { (x, y) -> free.remove(cellOf(x, y)) }

    val related = links(papers).flatMap { (a, b) -> listOf(a to b, b to a) }.groupBy({ it.first }, { it.second })
    papers.forEachIndexed { i, p ->
        if (p.id in spots || free.isEmpty()) return@forEachIndexed
        val near = related[p.id].orEmpty().mapNotNull { spots[it] }
        val cell = if (near.isEmpty()) free.first() else free.minBy { c ->
            val (cx, cy) = cellCentre(c, rows)
            near.sumOf { (x, y) -> hypot(cx - x, cy - y) }
        }
        free.remove(cell)
        // A little off the grid, the same every build, so auto-placed papers don't line up like a form
        val (cx, cy) = cellCentre(cell, rows)
        spots[p.id] = (cx + (i * 37 % 7 - 3) * 0.8) to (cy + (i * 53 % 5 - 2) * 1.0)
    }

    val placed = papers.mapIndexedNotNull { i, p ->
        val (x, y) = spots[p.id] ?: return@mapIndexedNotNull null
        Placed(p, i, x, y, p.rotation ?: TILTS[i % TILTS.size])
    }
    val wires = links(papers).mapNotNull { (a, b) ->
        val (x1, y1) = spots[a] ?: return@mapNotNull null
        val (x2, y2) = spots[b] ?: return@mapNotNull null
        Wire(a, b, sagPath(x1, y1, x2, y2))
    }
    return BoardLayout(placed, wires, rows)
}

/** Every linked pair once, whichever side declared it (and only pairs whose ends both exist). */
internal fun links(papers: List<IntelPaper>): List<Pair<String, String>> {
    val ids = papers.map { it.id }.toSet()
    return papers.flatMap { p ->
        p.linkedTo.filter { it in ids && it != p.id }.map { if (p.id < it) p.id to it else it to p.id }
    }.distinct()
}

/** Ids [id] is linked to, in board order. */
internal fun linkedIds(papers: List<IntelPaper>, id: String): List<String> {
    val other = links(papers).mapNotNull { (a, b) -> if (a == id) b else if (b == id) a else null }.toSet()
    return papers.map { it.id }.filter { it in other }
}

/** A cell's pin spot: centred across, a little below its top edge. */
private fun cellCentre(cell: Int, rows: Int): Pair<Double, Double> =
    ((cell % BOARD_COLS + 0.5) * 100.0 / BOARD_COLS) to ((cell / BOARD_COLS) * 100.0 / rows + 8)

/** String hangs between two pins: a quadratic curve whose middle drops further the longer the run. */
private fun sagPath(x1: Double, y1: Double, x2: Double, y2: Double): String {
    val sag = 3 + 0.12 * hypot(x2 - x1, y2 - y1)
    return "M${r(x1)} ${r(y1)}Q${r((x1 + x2) / 2)} ${r((y1 + y2) / 2 + sag)} ${r(x2)} ${r(y2)}"
}

private fun r(v: Double) = ((v * 100).roundToInt() / 100.0).toString()
