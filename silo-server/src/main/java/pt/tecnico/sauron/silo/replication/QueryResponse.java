package pt.tecnico.sauron.silo.replication;

/**
 * Class to encapsulate a query response from the server
 * with the replication data
 * @param <T> data type of the query value
 */
public class QueryResponse<T> {

    private VectorTimestamp newTS;

    private T data;

    public QueryResponse() {}

    public QueryResponse(T data, VectorTimestamp newTS) {
        this.data = data;
        this.newTS = newTS;
    }

    public VectorTimestamp getNewTS() {
        return newTS;
    }

    public void setNewTS(VectorTimestamp newTS) {
        this.newTS = newTS;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}