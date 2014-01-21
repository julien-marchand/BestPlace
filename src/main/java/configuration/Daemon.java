
package configuration;


//import org.simgrid.msg.*;
//import org.simgrid.msg.Process;
//import simulation.Main;

public class Daemon{
	private Task currentTask;
    private int load;
    public Daemon(XVM xvm, int load) {
		//super(xvm,"Daemon");
        this.load = load ;
        currentTask = new SimpleTask(this.getHost().getName()+"-daemon-0", this.getHost().getSpeed()*100, 0);
        //   currentTask.setBound(load);
    }
    private Host getHost() {
		// TODO Auto-generated method stub
		return null;
	}
//	public void main(String[] args){
//        int i = 1;
//        while(!Main.isEndOfInjection()) {
//            // TODO the binding is not yet available
//           // try {
//                currentTask.execute();
//            //} catch (HostFailureException e) {
//             //   e.printStackTrace();
//            //} catch (TaskCancelledException e) {
//                System.out.println("task cancelled");
//                suspend(); // Suspend the process
//            }
//            currentTask = new SimpleTask(this.getHost().getName()+"-daemon-"+(i++), this.getHost().getSpeed()*100, 0);
//            //currentTask.setBound(load);
//    }
	
    void suspend() {
		// TODO Auto-generated method stub
		
	}
	

    public double getRemaining(){
        return this.currentTask.getRemainingDuration();
    }
    public void updateLoad(int load){
       if(currentTask != null)
         currentTask.cancel();
       setBound(load);
       resume();
    }


	void resume() {
		// TODO Auto-generated method stub
		
	}
	public void setBound(int load) {
        this.load = load;
    }
	public void start() {
		// TODO Auto-generated method stub
		
	}
}
