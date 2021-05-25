package com.ef.mediaroutingengine.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "mediaRoutingDomains")
public class MediaRoutingDomain implements Serializable {

    @Id
    private UUID id;
    @NotNull
    @Size(min = 3, max = 110)
    private String name;
    @Size(max = 500)
    private String description;
    @NotNull
    private boolean interruptible;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInterruptible() {
        return interruptible;
    }

    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaRoutingDomain that = (MediaRoutingDomain) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MediaRoutingDomain{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", interruptible=" + interruptible
                + '}';
    }
}
