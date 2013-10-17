package pt.ua.ThiefCatch.Synchronism;

/**
 *
 * @author Paulo Gaspar
 */
class RPCRequest
{
    long routineID;
    Object[] arguments;
    RemoteResult RPCresult;

    public RPCRequest(long routineID, Object... args)
    {
        this.routineID = routineID;
        this.arguments = args;
        RPCresult = new RemoteResult();
    }

    public Object[] getArguments() {
        return arguments;
    }

    public long getRoutineID() {
        return routineID;
    }

    public Object getResult() {
        return RPCresult.get();
    }

    public void setResult(Object result) {
        this.RPCresult.set(result);
    }

    public RemoteResult getRemoteResult()
    {
        return RPCresult;
    }

}
