import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.*
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlin.random.Random
import LoremIpsum
import javafx.collections.ObservableList
import org.w3c.dom.events.MouseEvent
import java.awt.event.MouseListener

class NotesApp: Application() {
    val noteList = mutableListOf<Note>()
    var totalNotes: Int = noteList.size
    var filterCheck = false
    val notesPane = FlowPane()
    val statusBar = HBox(Label(totalNotes.toString()), Label(""))
    override fun start(stage: Stage) {
        val lorem = LoremIpsum()

        // Note Panes
        var scroll = ScrollPane(notesPane)
        val pane = BorderPane()

        // Status Bar Properties
        statusBar.padding = Insets(10.0); statusBar.spacing = 10.0
        notesPane.hgap = 10.0; notesPane.vgap = 10.0; notesPane.padding = Insets(10.0)

        // Buttons
        val addButton = Button("Add")
        addButton.prefWidth = 100.0
        addButton.onMouseClicked = EventHandler { editWindow(notesPane, false, statusBar) }

        val randomButton = Button("Random")
        randomButton.prefWidth = 100.0
        randomButton.onMouseClicked = EventHandler {
            addNote( lorem.getTitle(), lorem.getBody(), importance = Random.nextInt(5) == 1)
            updatePane(notesPane, filterCheck)
            updateStatusBar(statusBar, "Added Note ${noteList.size.toString()}", notesPane)
        }

        val filterButton = ToggleButton("!")


        val deleteButton = Button("Delete")
        deleteButton.prefWidth = 100.0
        deleteButton.onMouseClicked = EventHandler {
            var deleted = false
            var deletedId = -1
            for (note in noteList) {
                if (note.selected) {
                    deletedId = note.id
                    deleted = true
                } else if (deleted) {
                    note.id -= 1
                }
            }
            if (deleted) {noteList.removeAt(deletedId); updatePane(notesPane, filterCheck); updateStatusBar(statusBar, "Deleted Note #$deletedId", notesPane)} // To Avoid Concurrent Mod
        }

        val clearButton = Button("Clear")
        clearButton.prefWidth = 100.0
        clearButton.onMouseClicked = EventHandler {
            val importantToClear = mutableListOf<Int>()
            if (!filterCheck) { val numNotes = noteList.size.toString(); notesPane.children.clear(); noteList.clear(); updateStatusBar(statusBar, "Cleared $numNotes Notes", notesPane); updatePane(notesPane, filterCheck)}
            else {
                for (note in noteList) { if (note.importance) { importantToClear.add(note.id) } }
                var offset = 0 // Needed to correct the index in the removeAt  Method
                for (index in importantToClear) {noteList.removeAt(index - offset); offset += 1} // To Avoid Concurrent Mod
                notesPane.children.clear()
                updatePane(notesPane, filterCheck)
                updateStatusBar(statusBar, "Cleared ${importantToClear.size.toString()} Notes", notesPane)
            }
        }


        val searchBox = TextField()
        // Toolbar Properties
        val topToolBar = HBox(10.0)
        topToolBar.prefWidth(100.0)
        topToolBar.padding = Insets(10.0)
        topToolBar.children.addAll(addButton, randomButton, deleteButton, clearButton, filterButton, searchBox)

        // Panes
        scroll.isFitToHeight = true; scroll.isFitToWidth = true
        pane.bottom = statusBar
        pane.center = scroll
        pane.top = topToolBar


        // Scene Properties
        val scene = Scene(pane)
        stage.title = "A1 Notes (itejerom)"
        stage.scene = scene
        stage.minWidth = 400.0
        stage.minHeight = 400.0
        stage.height = 800.0
        stage.width = 600.0
        stage.show()


        // Button Actions
        filterButton.onMouseClicked = EventHandler {
            filterCheck = filterCheck.xor(true)
            updatePane(notesPane, filterCheck)
            updateStatusBar(statusBar, "Notes filtered by importance", notesPane)
        }
        searchBox.onKeyTyped = EventHandler {
            notesPane.children.clear()
            for (note in noteList) {
                if (note.title.contains(searchBox.text, ignoreCase = true) || (note.body.contains(
                        searchBox.text,
                        ignoreCase = true
                    ))
                )
                    if (filterCheck) { if (note.importance) notesPane.children.add(note.notePane)}
                    else { notesPane.children.add(note.notePane) }
            }
            updateStatusBar(statusBar, "", notesPane)
        }
    }

    private fun editWindow(paneEdit: FlowPane, editMode: Boolean, statusBar: HBox, noteToEdit: Note = Note("","", false, StackPane())) {
        val addStage = Stage()
        addStage.initModality(Modality.APPLICATION_MODAL); addStage.initStyle(StageStyle.UNDECORATED)
        val title = TextField(noteToEdit.title)
        val body = TextArea(noteToEdit.body); body.isWrapText = true
        val important = CheckBox("Important"); important.isSelected = noteToEdit.importance

        // Buttons
        val save = Button("Save"); save.minWidth = 100.0
        save.onMouseClicked = EventHandler {
            if (!editMode) {
                addNote(title.text, body.text, important.isSelected)
                updateStatusBar(statusBar, "Added Note ${noteList.size.toString()}", paneEdit)
            }
            else {
                noteToEdit.title = title.text; noteToEdit.body = body.text; noteToEdit.importance = important.isSelected; // Save to existing Note
                noteToEdit.notePane = addNote(title.text, body.text, important.isSelected, true)}
                updateStatusBar(statusBar, "Edited Note ${noteToEdit.id}", paneEdit);
                print(noteToEdit)
            updatePane(paneEdit, filterCheck); addStage.close()}

        val cancel = Button("Cancel"); cancel.minWidth = 100.0
        cancel.onMouseClicked = EventHandler { addStage.close() }

        val editForm = GridPane()
        editForm.hgap = 10.0; editForm.vgap = 10.0; editForm.alignment = Pos.CENTER

        editForm.add(Label("Title"), 0, 0); editForm.add(title, 1, 0);
        editForm.add(Label("Body"), 0, 1); editForm.add(body, 1, 1);
        editForm.add(important, 1,2)
        editForm.add(HBox(10.0, save, cancel), 1,3)
        editForm.columnConstraints.add(ColumnConstraints(50.0))

        val mode = if (editMode) Label("Edit Note") else Label("Add New Note")
        val editDialog = BorderPane()
        editDialog.padding = Insets(10.0);
        editDialog.top = mode
        editDialog.center = editForm


        val addNoteScene = Scene(editDialog)
        addStage.scene = addNoteScene
        addStage.height = 330.0; addStage.width = 400.0
        addStage.minHeight = 100.0; addStage.minWidth = 100.0
        addStage.show()
    }
    private fun addNote(title: String = "", body: String = "", importance: Boolean = false, editMode: Boolean = false) : StackPane {
        val titleLabel = Label(title)
        val bodyText = Label(body); bodyText.isWrapText = true
        val textBox = GridPane()
        textBox.add(titleLabel,0,0)
        textBox.add(bodyText,0,1)
        textBox.padding = Insets(10.0); textBox.maxWidth = 150.0; textBox.maxHeight = 200.0; textBox.vgap = 10.0

        var backColor = javafx.scene.shape.Rectangle(150.0, 200.0, Color.WHITE)
        if (importance)
            backColor = javafx.scene.shape.Rectangle(150.0, 200.0, Color.LIGHTYELLOW)
        val pane = StackPane(backColor, textBox)
        pane.style = "-fx-border-color: transparent"
        if (!editMode) {noteList.add(Note(title, body, importance, pane, false))}
        return pane
    }
    private fun updatePane (notesPane : FlowPane, filterCheck: Boolean) {
        if (filterCheck) {
            notesPane.children.clear()
            for (note in noteList) {
                if (note.importance) notesPane.children.add(note.notePane)
            }
        } else {
            notesPane.children.clear()
            for (note in noteList) {
                notesPane.children.add(note.notePane)
            }
        }
        var newIndex = 0
        for (note in noteList) {note.id = newIndex; newIndex++}
    }
    private fun updateStatusBar (statusBar : HBox, action: String, nodePane: FlowPane) {
        statusBar.children[0] = Label("(${nodePane.children.size.toString()}) of ${noteList.size.toString()}")
        statusBar.children[1] = Label(action)
    }
    inner class Note (titleString: String, bodyString: String, importanceBool: Boolean, pane: StackPane, selection: Boolean = false) {
        var id = noteList.size
        var title = titleString
        var body = bodyString
        var importance = importanceBool
        var notePane = pane
        var selected = selection
        init {
            notePane.onMouseClicked = EventHandler {
                if (selected) {editWindow(notesPane, true, statusBar, noteList.elementAt(id)); noteList.elementAt(id).selected = false}
                else {
                    selected = selected.xor(true)
                    if (selected) {
                        for (note in noteList) {
                            note.selected = false; note.notePane.style = "-fx-border-color: transparent"
                        }
                        selected = true; notePane.style = "-fx-border-color: blue"
                    } else {
                        selected = false; notePane.style = "-fx-border-color: transparent"
                    }
                }
            }
        }
    }
}