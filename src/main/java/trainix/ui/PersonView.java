package trainix.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import trainix.model.Person;
import trainix.service.PersonService;

@Route("")
public class PersonView extends VerticalLayout {
    private final PersonService service;

    private final TextField firstName = new TextField("First name");
    private final TextField lastName = new TextField("Last name");
    private final Button save = new Button("Save");

    private final Binder<Person> binder = new Binder<>(Person.class);
    private final Grid<Person> grid = new Grid<>(Person.class);

    @Autowired
    public PersonView(PersonService service) {
        this.service = service;

        // Explicit binding
        binder.forField(firstName).bind(Person::getFirstName, Person::setFirstName);
        binder.forField(lastName).bind(Person::getLastName, Person::setLastName);

        save.addClickListener(e -> savePerson());

        HorizontalLayout form = new HorizontalLayout(firstName, lastName, save);
        grid.setColumns("id", "firstName", "lastName");
        grid.setItems(service.findAll());

        add(form, grid);
    }

    private void savePerson() {
        Person p = new Person();
        if (binder.writeBeanIfValid(p)) {
            service.save(p);
            grid.setItems(service.findAll());
            binder.readBean(new Person()); // Clear form
        }
    }
}