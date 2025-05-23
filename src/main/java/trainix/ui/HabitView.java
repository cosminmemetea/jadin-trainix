package trainix.ui;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;

import org.springframework.beans.factory.annotation.Autowired;
import trainix.model.HabitEntity;
import trainix.service.HabitService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Route("habits")
@CssImport("./styles/shared-styles.css") // see note below
public class HabitView extends VerticalLayout {

    private final HabitService service;

    // --- Filters & Stats ---
    private final TextField nameFilter = new TextField("🔍 Search");
    private final MultiSelectComboBox<Integer> intervalFilter =
            new MultiSelectComboBox<>("📆 Interval (days)", List.of(1, 3, 7, 14, 30));
    private final H3 totalBadge   = new H3();
    private final H3 overdueBadge = new H3();

    // --- Chart & Grid ---
    private final Chart freqChart = new Chart(ChartType.COLUMN);
    private final Grid<HabitEntity> grid = new Grid<>(HabitEntity.class);

    // --- Form ---
    private final TextField    nameField     = new TextField("Habit name");
    private final IntegerField intervalField = new IntegerField("Interval (days)");
    private final Button       saveButton    = new Button("💾 Save");
    private final Button       doneButton    = new Button("✅ Done");
    private final Binder<HabitEntity> binder  = new Binder<>(HabitEntity.class);

    @Autowired
    public HabitView(HabitService service) {
        this.service = service;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        H2 header = new H2("🗓 Your Habit Dashboard");
        header.getStyle().set("margin", "var(--lumo-space-m)");
        add(header);

        configureFilters();
        configureForm();
        configureGrid();
        configureChart();

        SplitLayout split = new SplitLayout(buildLeftCard(), buildRightCard());
        split.setSizeFull();
        add(split);

        refresh(service.findAll());
    }

    private Div buildLeftCard() {
        Div card = new Div();
        card.addClassName("card");
        VerticalLayout layout = new VerticalLayout(nameField, intervalField, saveButton, doneButton);
        layout.setPadding(false);
        layout.setSpacing(true);
        card.add(layout);
        return card;
    }

    private Div buildRightCard() {
        Div card = new Div();
        card.addClassName("card");
        VerticalLayout layout = new VerticalLayout(
                new HorizontalLayout(nameFilter, intervalFilter),
                new HorizontalLayout(totalBadge, overdueBadge),
                freqChart,
                grid
        );
        layout.setPadding(false);
        layout.setSpacing(true);
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
                .asRequired("Interval is required")
                .withValidator(v -> v != null && v > 0, "Must be > 0")
                .bind(HabitEntity::getIntervalDays, HabitEntity::setIntervalDays);

        saveButton.addClickListener(e -> {
            HabitEntity h = new HabitEntity();
            try {
                binder.writeBean(h);
                service.save(h);
                binder.readBean(new HabitEntity());
                refresh(service.findAll());
                Notification.show("Saved!", 2000, Notification.Position.TOP_CENTER);
            } catch (ValidationException ve) {
                Notification.show("Fix errors first", 2000, Notification.Position.TOP_CENTER);
            }
        });

        doneButton.addClickListener(e -> {
            HabitEntity sel = grid.asSingleSelect().getValue();
            if (sel != null) {
                service.markDone(sel.getId());
                refresh(service.findAll());
                Notification.show("Marked done!", 2000, Notification.Position.TOP_CENTER);
            }
        });
    }

    private void configureGrid() {
        grid.setColumns("id","name","intervalDays","nextDue");
        grid.asSingleSelect().addValueChangeListener(e -> doneButton.setEnabled(e.getValue() != null));
    }

    private void configureChart() {
        freqChart.setWidthFull();
        freqChart.setHeight("200px");
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
        totalBadge.setText("Total habits: " + items.size());
        long overdue = items.stream()
                .filter(h -> h.getNextDue().isBefore(LocalDate.now()))
                .count();
        overdueBadge.setText("Overdue: " + overdue);
        updateChart(items);
    }

    private void updateChart(List<HabitEntity> items) {
        Map<Integer, Long> byInterval = items.stream()
                .collect(Collectors.groupingBy(
                        HabitEntity::getIntervalDays,
                        Collectors.counting()
                ));
        var sorted = byInterval.keySet().stream().sorted().toList();
        String[] categories = sorted.stream().map(Object::toString).toArray(String[]::new);
        Number[] values      = sorted.stream().map(byInterval::get).toArray(Number[]::new);

        Configuration cfg = freqChart.getConfiguration();
        cfg.setTitle("Habits by Frequency");
        cfg.getxAxis().setCategories(categories);
        cfg.setSeries(new ListSeries("Count", values));
    }
}
