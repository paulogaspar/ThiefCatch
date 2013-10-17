package pt.ua.ThiefCatch.Synchronism;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class responsable for the RPC calls to the police station.
 * In order to avoid overload in the police station, this actve entity
 * receives the RPC requests, and then calls the policeStation methods,
 * returning if necessary.
 *
 * @author Paulo Gaspar
 *
 */
public class Actor extends Thread
{

    /* To enlist available RPC routines and queued routines. */
    private HashMap<Long, RemoteRoutine> routines = new HashMap<Long, RemoteRoutine>();
    private Queue<RPCRequest> functionsToBeCalled  = new LinkedList<RPCRequest>();

    private boolean terminated = false;

    public synchronized void invokeAsyncProc(long rpcId, Object... args)
    {
        assert !terminated;
        assert exists(rpcId);

        RPCRequest routineRequest = new RPCRequest(rpcId, args);
        functionsToBeCalled.add(routineRequest);

        notify();
    }

    public void invokeSyncProc(long rpcId, Object... args)
    {
        assert !terminated;
        assert exists(rpcId);

        RPCRequest routineRequest;

        synchronized (this)
        {
            routineRequest = new RPCRequest(rpcId, args);
            functionsToBeCalled.add(routineRequest);
            
            notify();
        }

        /* Wait until command is over and result is set to not null. */
        routineRequest.getRemoteResult().await();
    }

    public synchronized RemoteResult invokeAsyncFunc(long rpcId, Object... args)
    {
        assert !terminated;
        assert exists(rpcId);

        /* Add new request to list of requests. */
        RPCRequest routineRequest = new RPCRequest(rpcId, args);
        functionsToBeCalled.add(routineRequest);
        notify();

        /* Return RemoteResult reference, so the caller can access the result
           when it is available and he wants/needs it. */
        return routineRequest.getRemoteResult();
    }

    public Object invokeSyncFunc(long rpcId, Object... args)
    {
        if (terminated) return null;
        RPCRequest routineRequest;

        synchronized (this)
        {
          assert exists(rpcId);

          routineRequest = new RPCRequest(rpcId, args);
          functionsToBeCalled.add(routineRequest);
          notify();
        }

        /* Wait for result. When available, function returns, thus synchronized. */
        return routineRequest.getResult();
    }

    @Override
    public synchronized void run()
    {
        while (!terminated || !functionsToBeCalled.isEmpty())
        {
            /* Is there a RPC request to serve? */
            if (functionsToBeCalled.isEmpty())
            {
                try {  this.wait(); }
                catch (InterruptedException ex) { System.out.println("Error in thread comunication."); System.exit(1); }
                continue; /* Verify again. */
            }

            /* Deal with request. */
            RPCRequest request = functionsToBeCalled.poll();
            RemoteRoutine routine = routines.get(request.getRoutineID());
            routine.setArgs(request.getArguments());
            routine.run();
            request.setResult(routine.result());
        }
    }

    public synchronized boolean exists(long rpcId)
    {
        return routines.containsKey(rpcId);
    }

    public synchronized void add(RemoteRoutine r)
    {
        assert !terminated;

        routines.put(r.id(), r);
    }

    public synchronized boolean terminated()
    {
        return (terminated == true);
    }

    public synchronized void terminate()
    {
        terminated = true;
        notify();
    }

}
