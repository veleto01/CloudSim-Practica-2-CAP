import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class ejercicio4 {

	public ejercicio4() {
	}

	public void run() {

		this.initCloudSim();

		this.createDataCenter();

		DatacenterBroker broker = this.createResources();

		this.simulate();

		this.printCloudletsResults(broker);

	}

	private void initCloudSim() {
		Log.printLine(">> Initializing cloudsim...");
		int num_user = 1; // number of cloud users
		Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date
													// and time.
		boolean traceFlag = false; // trace events
		CloudSim.init(num_user, calendar, traceFlag);
		Log.printLine(">> Cloudsim ready!");
	}

	private void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

	private void createDataCenter() {

		final int NUMERO_HOSTS = 3; // Queremos 3 hosts
		int mips = 1200;
		int ram = 16384; // 16 GB
		long almacenamiento = 1000000; // 1 TB
		long anchoBanda = 10000; // 10 Gbps
		List<Pe>[] listaCPUs = new List[NUMERO_HOSTS];
		Host[] host = new Host[NUMERO_HOSTS];
		List<Host> listaHosts = new ArrayList<Host>();

		for (int i = 0; i < NUMERO_HOSTS; i++) {
			listaCPUs[i] = new ArrayList<Pe>();
			listaCPUs[i].add(new Pe(0, new PeProvisionerSimple(mips)));
			if (i == 1) { // El host con id=1 contará con 4 procesadores
				listaCPUs[i].add(new Pe(1, new PeProvisionerSimple(mips)));
				listaCPUs[i].add(new Pe(2, new PeProvisionerSimple(mips)));
				listaCPUs[i].add(new Pe(3, new PeProvisionerSimple(mips)));
			}
			host[i] = new Host(i, new RamProvisionerSimple(ram), new BwProvisionerSimple(anchoBanda), almacenamiento,
					listaCPUs[i], new VmSchedulerTimeShared(listaCPUs[i]));
			listaHosts.add(host[i]);
		}

		String arquitectura = "x86";
		String so = "Linux";
		String vmm = "Xen";
		String nombre = "Datacenter_0";
		double zonaHoraria = 3.0;
		double costePorSeg = 0.007;
		double costePorMem = 0.005;
		double costePorAlm = 0.003;
		double costePorBw = 0.002;
		DatacenterCharacteristics caracteristicas = new DatacenterCharacteristics(arquitectura, so, vmm, listaHosts,
				zonaHoraria, costePorSeg, costePorMem, costePorAlm, costePorBw);
		Datacenter centroDeDatos = null;
		try {
			centroDeDatos = new Datacenter(nombre, caracteristicas, new VmAllocationPolicySimple(listaHosts),
					new LinkedList<Storage>(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private DatacenterBroker createResources() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("broker");
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.printLine(">> ERROR creating broker");
		}

		List<Vm> virtualMachines = new ArrayList<Vm>();

		for (int idx = 0; idx < 6; idx++) {
			virtualMachines.add(new Vm(virtualMachines.size(), broker.getId(), 400, 1, 2048, 1000, 40, "Xen",
					new CloudletSchedulerSpaceShared()));
		}

		broker.submitVmList(virtualMachines);

		List<Cloudlet> cloudlets = new ArrayList<Cloudlet>();

		UtilizationModel utilizationModel = new UtilizationModelFull();		
		
		
		broker.submitCloudletList(cloudlets);
		
		return broker;
	}

	private void simulate() {
		Log.printLine(">> Iniciando simulación...");
		CloudSim.startSimulation();
		Log.printLine(">> Simulación en curso...");
		CloudSim.stopSimulation();
		Log.printLine(">> Simulación finalizada.");
	}

	private void printCloudletsResults(DatacenterBroker broker) {
		this.printCloudletList(broker.getCloudletReceivedList());
	}
}
