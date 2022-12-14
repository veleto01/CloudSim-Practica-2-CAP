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

public class ejercicio1 {

	public ejercicio1() {
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
		List<Pe> listaCPUs = new ArrayList<Pe>();
		int mips = 500;
		listaCPUs.add(new Pe(0, new PeProvisionerSimple(mips)));
		listaCPUs.add(new Pe(1, new PeProvisionerSimple(mips)));
		int hostId = 0;
		int ram = 4096;
		long almacenamiento = 20000;
		long anchoBanda = 1000;

		Host host = new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(anchoBanda), almacenamiento,
				listaCPUs, new VmSchedulerSpaceShared(listaCPUs));

		List<Host> listaHosts = new ArrayList<Host>();
		listaHosts.add(host);

		String arquitectura = "x86";
		String so = "Linux";
		String vmm = "Xen";
		String nombre = "Datacenter_0";
		double zonaHoraria = 4.0;
		double costePorSeg = 0.01;
		double costePorMem = 0.01;
		double costePorAlm = 0.003;
		double costePorBw = 0.005;

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

		for (int idx = 0; idx < 4; idx++) {
			virtualMachines.add(new Vm(virtualMachines.size(), broker.getId(), 250, 1, 1024, 100, 4, "Xen",
					new CloudletSchedulerTimeShared()));
		}

		broker.submitVmList(virtualMachines);

		List<Cloudlet> cloudlets = new ArrayList<Cloudlet>();

		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet cloudlet1 = new Cloudlet(cloudlets.size(), 20000, 1, 1000000, 1500000, utilizationModel,
				utilizationModel, utilizationModel);
		cloudlet1.setUserId(broker.getId());
		cloudlets.add(cloudlet1);

		Cloudlet cloudlet2 = new Cloudlet(cloudlets.size(), 30000, 1, 2000000, 2200000, utilizationModel,
				utilizationModel, utilizationModel);
		cloudlet2.setUserId(broker.getId());
		cloudlets.add(cloudlet2);

		broker.submitCloudletList(cloudlets);

		return broker;
	}

	private void simulate() {
		Log.printLine(">> Iniciando simulaci??n...");
		CloudSim.startSimulation();
		Log.printLine(">> Simulaci??n en curso...");
		CloudSim.stopSimulation();
		Log.printLine(">> Simulaci??n finalizada.");
	}

	private void printCloudletsResults(DatacenterBroker broker) {
		this.printCloudletList(broker.getCloudletReceivedList());
	}
}
