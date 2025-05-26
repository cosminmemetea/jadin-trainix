package trainix.ui;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

import org.springframework.beans.factory.annotation.Autowired;
import trainix.model.HabitEntity;
import trainix.service.HabitService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route("habits")
@CssImport("./styles/shared-styles.css")
public class HabitView extends VerticalLayout {

    private final HabitService service;

    private final TextField nameFilter = new TextField("🔍 Search");
    private final MultiSelectComboBox<Integer> intervalFilter =
            new MultiSelectComboBox<>("📆 Interval", List.of(1,3,7,14,30));
    private final H3 totalBadge = new H3();
    private final H3 overdueBadge = new H3();
    private final Grid<HabitEntity> grid = new Grid<>(HabitEntity.class);
    private final TextField nameField = new TextField("Habit name");
    private final IntegerField intervalField = new IntegerField("Interval (days)");
    private final Button saveButton = new Button("💾 Save");
    private final Button doneButton = new Button("✅ Done");
    private final Binder<HabitEntity> binder = new Binder<>(HabitEntity.class);

    @Autowired
    public HabitView(HabitService service) {
        this.service = service;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        H2 header = new H2("🗓 Habit Dashboard");
        header.addClassName("header");
        add(header);

        configureFilters();
        configureForm();
        configureGrid();

        SplitLayout split = new SplitLayout(buildFormCard(), buildDataCard());
        split.setSizeFull();
        add(split);

        refresh(service.findAll());
    }

    private Div buildFormCard() {
        Div card = new Div();
        card.addClassName("card");

        VerticalLayout form = new VerticalLayout(nameField, intervalField, saveButton, doneButton);
        form.setPadding(false); form.setSpacing(true);
        card.add(form);
        return card;
    }

    private Div buildDataCard() {
        Div card = new Div();
        card.addClassName("card");

        VerticalLayout layout = new VerticalLayout(
                new HorizontalLayout(nameFilter, intervalFilter),
                new HorizontalLayout(totalBadge, overdueBadge),
                grid
        );
        layout.setPadding(false); layout.setSpacing(true);
        grid.setHeight("50vh");
        card.add(layout);
        return card;
    }

    private void configureFilters() {
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(e -> refresh(filteredHabits()));

        intervalFilter.setClearButtonVisible(true);
        intervalFilter.addValueChangeListener(e -> refresh(filteredHabits()));
    }

    private void configureForm() {
        binder.forField(nameField)
                .asRequired("Name is required")
                .bind(HabitEntity::getName, HabitEntity::setName);

        binder.forField(intervalField)
                .asRequired("Interval needed")
                .withValidator(i -> i != null && i > 0, "Must be > 0")
                .bind(HabitEntity::getIntervalDays, HabitEntity::setIntervalDays);

        saveButton.addClickListener(e -> saveHabit());
        doneButton.addClickListener(e -> markHabitDone());
        doneButton.setEnabled(false);  // until a row is selected
    }

    private void configureGrid() {
        grid.removeAllColumns();
        grid.addColumn(HabitEntity::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(HabitEntity::getName).setHeader("Habit").setAutoWidth(true);
        grid.addColumn(HabitEntity::getIntervalDays).setHeader("Interval").setAutoWidth(true);
        grid.addColumn(h -> h.getNextDue().toString()).setHeader("Next Due").setAutoWidth(true);

        grid.addComponentColumn(h -> {
            ProgressBar pb = new ProgressBar(0, 66, h.getTimesCompleted());
            pb.setWidth("150px");
            Span lbl = new Span(h.getTimesCompleted() + "/66");
            return new HorizontalLayout(pb, lbl);
        }).setHeader("Streak").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(e -> doneButton.setEnabled(e.getValue() != null));
    }

    private List<HabitEntity> filteredHabits() {
        String term = nameFilter.getValue().trim().toLowerCase();
        Set<Integer> sel = intervalFilter.getValue();
        return service.findAll().stream()
                .filter(h -> h.getName().toLowerCase().contains(term))
                .filter(h -> sel.isEmpty() || sel.contains(h.getIntervalDays()))
                .collect(Collectors.toList());
    }

    private void refresh(List<HabitEntity> items) {
        grid.setItems(items);

        totalBadge.setText("Total: " + items.size());
        long overdue = items.stream()
                .filter(h -> h.getNextDue().isBefore(LocalDate.now()))
                .count();
        overdueBadge.setText("Overdue: " + overdue);
    }

    private void saveHabit() {
        HabitEntity h = new HabitEntity();
        try {
            binder.writeBean(h);
            service.save(h);
            binder.readBean(new HabitEntity());
            refresh(service.findAll());
            Notification.show("Saved!", 2000, Position.TOP_CENTER);
        } catch (ValidationException ex) {
            Notification.show("Fix form errors", 2000, Position.TOP_CENTER);
        }
    }

    private void markHabitDone() {
        HabitEntity sel = grid.asSingleSelect().getValue();
        if (sel != null) {
            service.markDone(sel.getId());
            refresh(service.findAll());
            int done = sel.getTimesCompleted();
            if (done >= 66) {
                Notification.show("🎉 Congrats! You've formed the habit!", 4000, Position.TOP_CENTER);
            } else {
                Notification.show("Keep going! " + (66 - done) + " days to go.", 3000, Position.TOP_CENTER);
            }
        }
    }
}
