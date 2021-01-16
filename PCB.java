
import java.util.ArrayList;

/**
 * Author : Brian Klein 
 * Date : 2/14/19 
 * Description : Process Control Block object to be used in CPU Scheduling Simulation.
 */
public class PCB {

    String processID;
    ArrayList<Integer> cpuBurstList;
    ArrayList<Integer> ioBurstList;

    public PCB() {
    }

    public PCB(String processID, ArrayList cpuBurstList, ArrayList ioBurstList) {
        this.processID = processID;
        this.cpuBurstList = cpuBurstList;
        this.ioBurstList = ioBurstList;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public ArrayList getCpuBurstList() {
        return cpuBurstList;
    }

    public void setCpuBurstList(ArrayList cpuBurstList) {
        this.cpuBurstList = cpuBurstList;
    }

    public ArrayList getIoBurstList() {
        return ioBurstList;
    }

    public void setIoBurstList(ArrayList ioBurstList) {
        this.ioBurstList = ioBurstList;
    }

    public int getCpuBurst() {
        return this.cpuBurstList.get(0);
    }

    public void setCpuBurst(Integer cpuBurst) {
        this.cpuBurstList.set(0, cpuBurst);
    }

    public int getIoBurst() {
        return this.ioBurstList.get(0);
    }

    public void setIoBurst(Integer ioBurst) {
        this.ioBurstList.set(0, ioBurst);
    }
    
    @Override
    public String toString() {
        return "" + processID;
    }

}//end PCB
