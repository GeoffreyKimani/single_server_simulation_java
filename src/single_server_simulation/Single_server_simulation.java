/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package single_server_simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import static java.lang.Math.log;
import java.util.Scanner;

/**
 *
 * @author jeffkim
 */
public class Single_server_simulation {

//    define limit of the queue length 
     public static final int Q_LIMIT = 100;
     
//     define the server status 
     int BUSY = 1;
     int IDLE = 0;
     
//     define the variales for the system 
     public int next_event_type, num_custs_delayed, num_delays_required, num_events, num_in_q, server_status;
     
     public float area_num_in_q, area_server_status, mean_interarrival, 
             mean_service, time, time_last_event, total_of_delays;   
     
     public float [] time_arrival = new float[Q_LIMIT + 1] ; //Q: WHY IS TIME_NEXT_EVENT OF TYPE FLOAT?
     public float [] time_next_event = new float[3];
     
     public void initialize(){
    //initialize the simulation clock
        time = 0;

    // initialiize state variables
        server_status = IDLE;
        num_in_q = 0;
        time_last_event = 0;


    //initialize statistical counters
        num_custs_delayed = 0;
        total_of_delays = 0;
        area_num_in_q = 0;
        area_server_status =0;
        
            /* Initialize event list.
    Since no customers are present, the
    departure (service completion) event is eliminated from
    consideration. The end-simulation event (type 3) is SCheduled
    for time time_end. */

        time_next_event[1] = (float) (time + expon(mean_interarrival));
        time_next_event [2] = (float) 1.0e+30;

     }
     
     public void timing(){
        int i;
        float min_time_next_event = (float) 1.0e+29;
        next_event_type = 0;
        
        // determine the event type of the next even to occur
        for(i=1; i <= num_events; i++){
            if(time_next_event[i] < min_time_next_event){
                min_time_next_event =  time_next_event[i];
                next_event_type = i;
            }
        }
            
        //check to see whether the event list is empty
        if(next_event_type == 0){
            //event list is empty, so stop the simulation
            System.out.printf("\nEvent list at time %f", time);
            // OUTPUT TO A FILE
            System.exit(0);
        }

    // the event list is not empty so advance the simulation clock
        time = min_time_next_event;
    }
     
     public void arrive(){
         float delay;
         
//         schedule for the next arrival
        time_next_event[1] = (float) (time + expon(mean_interarrival));
        
//        check the server status to determine whether to incerement queue
        if (server_status == BUSY){
            // increment the queue
            num_in_q++;
            
            // also check to see if the queue has gone above the set limit
            if (num_in_q > Q_LIMIT){
                //queue has overflown, so stop the simulation
                System.out.printf("Overflow of the array, time arrival at time %f", time);
//                OUTPUT TO A FILE
                System.exit(0);
            }
            //else there is still room in the queue, so store the time of arrival of the customer
            time_arrival[num_in_q] = time;
        }else{
            //the server is idle, customer enters service immediately
            delay = 0;
            total_of_delays += delay;

            // increment number of customers delayed and change the server status
            num_custs_delayed++;
            server_status = BUSY;

            //schedule service completion.
            time_next_event[2] = (float)(time + expon(mean_service));
        }
    
     }
     
     public void depart(){
        int i;
        float delay;

        //check to see whether the queue is empty
        if (num_in_q == 0){
            // make the server idle. eliminate the departure event from consideration
            server_status = IDLE;
            time_next_event[2] = (float) 1.0e+30;
        }
        else{
        //queue is not empty, so decrement the number of customers in queue.
        -- num_in_q;

        // compute the delay of customer who is beginning service and update total delay accumulator
        delay = time - time_arrival[1]; //////
        total_of_delays += delay;

        //increnent the number of customers delayed and schedule departure.
        num_custs_delayed++;
        time_next_event[2] = (float)(time + expon(mean_service));

        // move customers in queue (if any) one place up
        for(i = 1; i <= num_in_q; ++i){
            time_arrival[i] = time_arrival[i+1];
        }
        }
     }
     
    public void update_time_avg_stats(){
        //update area accumulators for time-average statistics
        float time_since_last_event;

        //compute time since last event and update last-time-event marker
        time_since_last_event = time - time_last_event;
        time_last_event = time;

        /* Update area under number-in-queue function. */
        area_num_in_q += num_in_q * time_since_last_event;

        /*'Update area under server-busy indicator function. */
        area_server_status += server_status * time_since_last_event;
    }
    
    public void report() throws FileNotFoundException{   
            PrintWriter writer = new PrintWriter("C:\\Users\\jeffkim\\Documents\\NetBeansProjects\\single_server_simulation\\src\\single_server_simulation\\output_file.txt");        
            
            writer.format("\n\nAverage delay in queue%11.3f minutes\n\n", (total_of_delays/num_custs_delayed));
            writer.format("\n\nAverage number in queue%10.3f\n\n", (area_num_in_q/time));
            writer.format("\n\nServer Utilization%15.3f minutes\n\n", (area_server_status/time));
            writer.format("\n\nNumber of delays completed%7d\n\n", num_custs_delayed);
            writer.format("Time simulation ended %12.3f", time);
            
            writer.close();
                
    }
    
    double expon(float mean){
        return -mean * log(Math.random());
    }
     
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        Single_server_simulation simulator =  new Single_server_simulation();
        //  call the initialize function
        simulator.initialize();
        
        simulator.num_events = 2;
      
//        open input and output files here
        File input_file = new File("C:\\Users\\jeffkim\\Documents\\NetBeansProjects\\single_server_simulation\\src\\single_server_simulation\\input_file.txt");
        if (input_file.exists()){
            Scanner file_scanner  = new Scanner(input_file);
            
//            read input parameters
            float input_values[] = new float [3]; 
            int i = 0;
            while (file_scanner.hasNext()) {
                input_values[i] = file_scanner.nextFloat();
                i++;                
            }
            
//            assign values
            simulator.mean_interarrival = input_values[0];
            simulator.mean_service = input_values[1];
            simulator.num_delays_required = (int) input_values[2];
                     
        }else{
            System.err.println("Sorry File Not Found");
        }
            
        System.out.println("Check output file");
        
            PrintWriter writer = new PrintWriter("C:\\Users\\jeffkim\\Documents\\NetBeansProjects\\single_server_simulation\\src\\single_server_simulation\\output_file.txt");        
            
            //   report header with input parameters
            writer.println("*** SINGLE SERVER QUEUING SYSTEM ***");
            writer.format("Mean inter-arrival time%11.3f minutes\n\n", simulator.mean_interarrival);
            writer.format("Mean service time%16.3f minutes\n\n" ,  simulator.mean_service);
            writer.format("Number of customers %14d minutes\n\n", simulator.num_delays_required);
            writer.close();
        
            
            /* run the simulation until it terminates after an end-simulation event occurs*/
        while (simulator.num_custs_delayed < simulator.num_delays_required)
        {
            
            //determine the next event
            simulator.timing();
                
            //update the time-average statistical accumulators
            simulator.update_time_avg_stats();
          
            //invoke the next event function
            switch(simulator.next_event_type)
            {
            case 1:
                simulator.arrive();
                break;
            case 2:
                simulator.depart();
                break;
            }
        }
          
//     call the report function  
        simulator.report();
          
    }
}
