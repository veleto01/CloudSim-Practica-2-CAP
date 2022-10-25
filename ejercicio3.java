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

public class ejercicio3 {

	public ejercicio3() {
	}

	public void run() {

		this.initCloudSim();

		this.createDataCenter();

		DatacenterBroker broker1 = this.createResources(1);


		this.simulate();

		this.printCloudletsResults(broker1);


	}

	private void initCloudSim() {
		Log.printLine(">> Initializing cloudsim...");
		int num_user = 4; // number of cloud users
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
		int mips = 1200;
		listaCPUs.add(new Pe(0, new PeProvisionerSimple(mips)));
		listaCPUs.add(new Pe(1, new PeProvisionerSimple(mips)));
		listaCPUs.add(new Pe(2, new PeProvisionerSimple(mips)));
		listaCPUs.add(new Pe(3, new PeProvisionerSimple(mips)));

		int ram = 24576; // 24 GB
		long almacenamiento = 2000000; // 2 TB
		long anchoBanda = 10000; // 10 Gbps

		final int NUMERO_HOSTS = 5; // Queremos 5 hosts
		Host[] host = new Host[NUMERO_HOSTS];
		List<Host> listaHosts = new ArrayList<Host>();
		for (int i = 0; i < NUMERO_HOSTS; i++) {
			host[i] = new Host(i, new RamProvisionerSimple(ram), new BwProvisionerSimple(anchoBanda), almacenamiento,
					listaCPUs, new VmSchedulerSpaceShared(listaCPUs));
			listaHosts.add(host[i]);
		}

		String arquitectura = "x86";
		String so = "Linux";
		String vmm = "Xen";
		String nombre = "Datacenter_0";
		double zonaHoraria = 2.0;
		double costePorSeg = 0.01;
		double costePorMem = 0.005;
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

	private DatacenterBroker createResources(int n) {

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("broker");
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.printLine(">> ERROR creating broker");
		}

		List<Vm> virtualMachines = new ArrayList<Vm>();

		for (int idx = 0; idx < 3; idx++) {
			virtualMachines.add(new Vm(virtualMachines.size(), broker.getId(), 600, 2, 4096, 1000, 20, "Xen",
					new CloudletSchedulerSpaceShared()));
		}

		broker.submitVmList(virtualMachines);

		List<Cloudlet> cloudlets = new ArrayList<Cloudlet>();

		UtilizationModel utilizationModel = new UtilizationModelFull();

		switch (n) {
		case 1:
			for (int contador = 0; contador < 15; contador++) {
				Cloudlet cloudlet = new Cloudlet(contador, 45000, 1, 1000000, 1500000, utilizationModel,
						utilizationModel, utilizationModel);
				cloudlet.setUserId(broker.getId());
				cloudlets.add(cloudlet);
			}
			broker.submitCloudletList(cloudlets);
			
			break;
		case 2:
			for (int contador = 16; contador < 31; contador++) {
				Cloudlet cloudlet = new Cloudlet(contador, 45000, 1, 1000000, 1500000, utilizationModel,
						utilizationModel, utilizationModel);
				cloudlet.setUserId(broker.getId());
				cloudlets.add(cloudlet);
			}
			broker.submitCloudletList(cloudlets);
			
			break;
		case 3:
			for (int contador = 32; contador < 46; contador++) {
				Cloudlet cloudlet = new Cloudlet(contador, 45000, 1, 1000000, 1500000, utilizationModel,
						utilizationModel, utilizationModel);
				cloudlet.setUserId(broker.getId());
				cloudlets.add(cloudlet);
			}
			broker.submitCloudletList(cloudlets);
			
			break;
		case 4:
			for (int contador = 47; contador < 61; contador++) {
				Cloudlet cloudlet = new Cloudlet(contador, 45000, 1, 1000000, 1500000, utilizationModel,
						utilizationModel, utilizationModel);
				cloudlet.setUserId(broker.getId());
				cloudlets.add(cloudlet);
			}
			broker.submitCloudletList(cloudlets);
			
			break;
		}

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
