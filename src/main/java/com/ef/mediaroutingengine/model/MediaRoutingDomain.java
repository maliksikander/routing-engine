package com.ef.mediaroutingengine.model;

import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The type Media routing domain.
 */
@Document(value = "mediaRoutingDomains")
public class MediaRoutingDomain implements Serializable {
    /**
     * The ID.
     */
    @Id
    private String id;
    /**
     * The Name.
     */
    @NotNull
    @Size(min = 3, max = 110)
    private String name;
    /**
     * The Description.
     */
    @Size(max = 500)
    private String description;
    /**
     * The Interruptible.
     */
    @NotNull
    private boolean interruptible;
    /**
     * The Max requests.
     */
    @Min(value = 1, message = "Maximum requests should be greater than 0")
    private int maxRequests;

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Is interruptible boolean.
     *
     * @return the boolean
     */
    public boolean isInterruptible() {
        return interruptible;
    }

    /**
     * Sets interruptible.
     *
     * @param interruptible the interruptible
     */
    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }

    /**
     * Gets max requests.
     *
     * @return the max requests
     */
    public int getMaxRequests() {
        return maxRequests;
    }

    /**
     * Sets max requests.
     *
     * @param maxRequests the max requests
     */
    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
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
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", interruptible=" + interruptible
                + ", maxRequests=" + maxRequests
                + '}';
    }
}
