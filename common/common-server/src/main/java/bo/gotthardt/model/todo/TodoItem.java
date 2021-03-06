package bo.gotthardt.model.todo;

import bo.gotthardt.AccessibleBy;
import bo.gotthardt.Persistable;
import bo.gotthardt.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Bo Gotthardt
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TodoItem implements Persistable, AccessibleBy<User> {
    @Id
    private long id;
    private String name;
    private boolean completed = false;
    @ManyToOne
    @JsonIgnore
    private TodoList todoList;

    public TodoItem(String name) {
        this.name = name;
    }

    @Override
    public boolean isAccessibleBy(User principal) {
        return todoList.getOwner().getId() == principal.getId();
    }
}
