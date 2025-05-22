package trainix.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import trainix.model.HabitEntity;
import trainix.service.HabitService;

@Route("habits")
public class HabitView extends VerticalLayout {
    private final HabitService service;
    private final Grid<HabitEntity> grid = new Grid<>(HabitEntity.class);
    private final Binder<HabitEntity> binder = new Binder<>(HabitEntity.class);

    // form fields
    private final TextField    nameField     = new TextField("Habit name");
    private final IntegerField intervalField = new IntegerField("Every N days");
    private final Button       saveButton    = new Button("Save");
    private final Button       doneButton    = new Button("Mark Done");

    @Autowired
    public HabitView(HabitService service) {
        this.service = service;

        // bind fields to HabitEntity properties
        binder.forField(nameField)
                .bind(HabitEntity::getName, HabitEntity::setName);
        binder.forField(intervalField)
                .bind(HabitEntity::getIntervalDays, HabitEntity::setIntervalDays);

        saveButton.addClickListener(e -> {
            HabitEntity h = new HabitEntity();
            binder.writeBeanIfValid(h);
            service.save(h);
            updateGrid();
        });

        doneButton.addClickListener(e -> {
            HabitEntity sel = grid.asSingleSelect().getValue();
            if (sel != null) {
                service.markDone(sel.getId());
                updateGrid();
            }
        });

        HorizontalLayout form = new HorizontalLayout(
                nameField, intervalField, saveButton, doneButton);

        grid.setColumns("id","name","intervalDays","nextDue");
        add(form, grid);

        updateGrid();
    }

    private void updateGrid() {
        grid.setItems(service.findAll());
    }
}
