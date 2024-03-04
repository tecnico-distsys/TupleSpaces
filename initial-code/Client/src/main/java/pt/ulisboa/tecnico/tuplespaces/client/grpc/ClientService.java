package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

public class ClientService {

    /* TODO: This class should implement the front-end of the replicated TupleSpaces service 
        (according to the Xu-Liskov algorithm)*/

    OrderedDelayer delayer;

    public ClientService(int numServers) {

        /* TODO: create channel/stub for each server */
    
        /* The delayer can be used to inject delays to the sending of requests to the 
            different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(numServers);
    }

    /* This method allows the command processor to set the request delay assigned to a given server */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);

        /* TODO: Remove this debug snippet */
        System.out.println("[Debug only]: After setting the delay, I'll test it");
        for (Integer i : delayer) {
          System.out.println("[Debug only]: Now I can send request to stub[" + i + "]");
      }
      System.out.println("[Debug only]: Done.");
    }

    /* TODO: individual methods for each remote operation of the TupleSpaces service */

    /* Example: How to use the delayer before sending requests to each server 
     *          Before entering each iteration of this loop, the delayer has already 
     *          slept for the delay associated with server indexed by 'id'.
     *          id is in the range 0..(numServers-1).
    
        for (Integer id : delayer) {
            //stub[id].some_remote_method(some_arguments);
        }

    */
    
}
