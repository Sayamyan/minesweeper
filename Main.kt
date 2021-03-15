package minesweeper

import java.util.*
import java.util.Stack

import java.util.ArrayList


fun main() {
    val game = Game()
    game.start()
}

class Cell {
    var isMine = false
    var isMarked = false
    var isOpened = false
    var row = 0
    var col = 0
}

class Field(mines: Int) {
    private val SIZE = 9
    private val UNKNOWN = '.'
    private val EMPTY = '/'
    private val MINE = 'X'
    private val MARKED = '*'
    var cells: Array<Array<Cell?>> = Array(SIZE) { arrayOfNulls(SIZE) }
    var minesArray: MutableList<Cell?> = ArrayList()
    var stack = Stack<Cell?>()

    fun printField() {
        println(" │123456789│\n—│—————————│")
        for (i in cells.indices) {
            print("${i + 1}|")
            for (j in cells.indices) {
                if (cells[i][j]!!.isOpened) {
                    if (cells[i][j]!!.isMine) {
                        print(MINE)
                    } else {
                        val numberOfMine = checkMineAround(i, j)
                        if (numberOfMine == 0) {
                            print(EMPTY)
                        } else {
                            print(numberOfMine)
                        }
                    }
                } else if (cells[i][j]!!.isMarked) {
                    print(MARKED)
                } else {
                    print(UNKNOWN)
                }
            }
            println("|")
        }
        println("—│—————————│")
    }

    fun checkFirstTurn(row: Int, col: Int) {
        if (cells[row][col]!!.isMine) {
            for (i in 0 until SIZE * SIZE) {
                val findingRow = i / cells.size
                val findingCol = i % cells.size
                if (findingRow != row || findingCol != col) {
                    if (!cells[findingRow][findingCol]!!.isMine) {
                        cells[findingRow][findingCol]!!.isMine = true
                        cells[row][col]!!.isMine = false
                        minesArray.remove(cells[row][col])
                        minesArray.add(cells[findingRow][findingCol])
                        break
                    }
                }
            }
        }
    }

    private fun checkMineAround(row: Int, col: Int): Int {
        var result = 0
        if (cells[row][col]!!.isMine) {
            return -1
        }
        var upShift = 1
        var downShift = 1
        var leftShift = 1
        var rightShift = 1
        if (row == 0) {
            upShift = 0
        }
        if (row == cells.size - 1) {
            downShift = 0
        }
        if (col == 0) {
            leftShift = 0
        }
        if (col == cells.size - 1) {
            rightShift = 0
        }
        for (i in row - upShift..row + downShift) {
            for (j in col - leftShift..col + rightShift) {
                if (cells[i][j]!!.isMine) {
                    result++
                }
            }
        }
        return result
    }

    private fun checkAllAround(row: Int, col: Int) {
        var upShift = 1
        var downShift = 1
        var leftShift = 1
        var rightShift = 1
        if (row == 0) {
            upShift = 0
        }
        if (row == cells.size - 1) {
            downShift = 0
        }
        if (col == 0) {
            leftShift = 0
        }
        if (col == cells.size - 1) {
            rightShift = 0
        }
        for (i in row - upShift..row + downShift) {
            for (j in col - leftShift..col + rightShift) {
                if (!cells[i][j]!!.isOpened) {
                    if (checkMineAround(i, j) == 0) {
                        cells[i][j]!!.isOpened = true
                        stack.push(cells[i][j])
                    } else if (checkMineAround(i, j) > 0) {
                        cells[i][j]!!.isOpened = true
                    }
                }
            }
        }
    }

    fun openArea(row: Int, col: Int) {
        if (checkMineAround(row, col) > 0) {
            cells[row][col]!!.isOpened = true
        } else {
            cells[row][col]!!.isOpened = true
            checkAllAround(row, col)
            while (!stack.empty()) {
                val nextCell = stack.pop()
                checkAllAround(nextCell!!.row, nextCell.col)
            }
        }
    }

    fun setAllBombOpened() {
        for (each in minesArray) {
            each!!.isOpened = true
        }
    }

    fun markCellAsMine(cell: Cell): Int {
        return if (!cell.isOpened) {
            if (!cell.isMarked) {
                cell.isMarked = true
                1
            } else {
                cell.isMarked = false
                -1
            }
        } else {
            println("There is a number here!")
            0
        }
    }

    init {
        var minesQuantity = 0
        for (i in cells.indices) {
            for (j in cells.indices) {
                cells[i][j] = Cell()
                cells[i][j]!!.row = i
                cells[i][j]!!.col = j
            }
        }
        while (minesQuantity < mines) {
            val random = Random()
            val row = random.nextInt(SIZE)
            val col = random.nextInt(SIZE)
            if (!cells[row][col]!!.isMine) {
                cells[row][col]!!.isMine = true
                minesArray.add(cells[row][col])
                minesQuantity++
            }
        }
    }
}

class Game {
    var field: Field? = null
    var result: Result = Result(this)
    var markedMines = 0
    var isFirstTurn = true
    var scanner = Scanner(System.`in`)

    fun start() {
        print("How many mines do you want on the field? ")
        val mines = scanner.nextInt()
        field = Field(mines)
        field!!.printField()
        var isExploded = false
        while (result.isNotGameOver) {
            print("Set/unset mines marks or claim a cell as free: ")
            val col = scanner.next().toInt() - 1
            val row = scanner.next().toInt() - 1
            val commandMarkFree = scanner.next().equals("free")
            if (commandMarkFree) {
                if (isFirstTurn) {
                    field!!.checkFirstTurn(row, col)
                    isFirstTurn = false
                }
                isExploded = result.checkExplodes(field!!.cells[row][col]!!)
                if (isExploded) {
                    field!!.printField()
                    break
                } else {
                    field!!.openArea(row, col)
                }
            } else {
                markedMines += field!!.markCellAsMine(field!!.cells[row][col]!!)
            }
            field!!.printField()
        }
        if (isExploded) {
            println("You stepped on a mine and failed!")
        } else {
            println("Congratulations! You found all mines!")
        }
    }
}

class Result(var game: Game) {
    private val isAllMarkedMinesFound: Boolean
        get() {
            var isAllMinesMarked = true
            for (mine in game.field!!.minesArray) {
                if (!mine!!.isMarked) {
                    isAllMinesMarked = false
                    break
                }
            }
            val isMinesQuantity = game.markedMines == game.field!!.minesArray.size
            return isAllMinesMarked && isMinesQuantity
        }
    private val isNoOpenMinesLeft: Boolean
        get() {
            for (i in game.field!!.cells.indices) {
                for (j in game.field!!.cells.indices) {
                    if (!game.field!!.cells[i][j]!!.isOpened && !game.field!!.cells[i][j]!!.isMine) {
                        return false
                    }
                }
            }
            return true
        }
    val isNotGameOver: Boolean
        get() = !(isAllMarkedMinesFound || isNoOpenMinesLeft)

    fun checkExplodes(cell: Cell): Boolean {
        return if (cell.isMine) {
            game.field!!.setAllBombOpened()
            true
        } else {
            false
        }
    }
}