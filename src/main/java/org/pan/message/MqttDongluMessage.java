package org.pan.message;

public class MqttDongluMessage {


    private Long id;
    private MessageDirection direction;
    private String data;

    public MqttDongluMessage() {
    }

    public Long getId() {
        return id;
    }

    public MessageDirection getDirection() {
        return direction;
    }

    public String getData() {
        return data;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDirection(MessageDirection direction) {
        this.direction = direction;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MqttDongluMessage))
            return false;
        MqttDongluMessage other = (MqttDongluMessage) o;
        if (!other.canEqual(this))
            return false;
        Object this$id = getId();
        Object other$id = other.getId();
        if (this$id != null ? !this$id.equals(other$id) : other$id != null)
            return false;
        Object this$direction = getDirection();
        Object other$direction = other.getDirection();
        if (this$direction != null ? !this$direction.equals(other$direction) : other$direction != null)
            return false;
        Object this$data = getData();
        Object other$data = other.getData();
        return this$data != null ? this$data.equals(other$data) : other$data == null;
    }

    protected boolean canEqual(Object other) {
        return other instanceof MqttDongluMessage;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $id = getId();
        result = result * 59 + ($id != null ? $id.hashCode() : 43);
        Object $direction = getDirection();
        result = result * 59 + ($direction != null ? $direction.hashCode() : 43);
        Object $data = getData();
        result = result * 59 + ($data != null ? $data.hashCode() : 43);
        return result;
    }

    public String toString() {
        return (new StringBuilder()).append("MqttDongluMessage(id=").append(getId()).append(", direction=").append(getDirection()).append(", data=").append(getData()).append(")").toString();
    }

}
