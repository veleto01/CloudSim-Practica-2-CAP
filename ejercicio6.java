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
import java.util.Random;

public class ejercicio6 {

	public ejercicio6() {
	}

	public void run() {

		this.initCloudSim();

		this.createDataCenter();

		DatacenterBroker broker1 = this.createResources(1);

		DatacenterBroker broker2 = this.createResources(2);

		DatacenterBroker broker3 = this.createResources(3);

		//DatacenterBroker broker1 = this.createResources(3);

		//DatacenterBroker broker2 = this.createResources(2);

		//DatacenterBroker broker3 = this.createResources(1);
		this.simulate();

		//this.printCloudletsResults(broker1);
		//this.printCloudletsResults(broker2);
		//this.printCloudletsResults(broker3);

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

	public class VmAllocationPolicyRandom extends VmAllocationPolicySimple {
		public VmAllocationPolicyRandom(List<? extends Host> list) {
			super(list);
		}

		@Override
		public boolean allocateHostForVm(Vm vm) {
			int requiredPes = vm.getNumberOfPes();
			boolean result = false;
			int tries = 0;

			List<Integer> Comprobaridx = new ArrayList<Integer>();
			List<Integer> freePesTmp = new ArrayList<Integer>();

			int min = 1;
			final int NUMERO_HOSTS = 3;
			int posicion;
			int idx = 0;

			do {
				Random random = new Random();
				idx = random.nextInt(NUMERO_HOSTS - 1 + min);
				posicion = Comprobaridx.indexOf(idx);
			} while (posicion >= 0);

			Comprobaridx.add(idx);

			for (Integer freePes : getFreePes()) {
				freePesTmp.add(freePes);
			}
			if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
				do {// we still trying until we find a host or until we try all of them

					Host host = getHostList().get(idx);
					result = host.vmCreate(vm);
					if (result) { // if vm were succesfully created in the host
						getVmTable().put(vm.getUid(), host);
						getUsedPes().put(vm.getUid(), requiredPes);
						getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
						result = true;
						break;
					} else {
						freePesTmp.set(idx, Integer.MIN_VALUE);
					}
					tries++;
				} while (!result && tries < getFreePes().size());
			}

			return result;
		}
	}

	private void createDataCenter() {

		final int Host_1 = 16;
		final int Host_2 = 4;
		int mips_1=2000;
		int mips_2=2400;
		int ram_1=8192;
		int ram_2=24576;
		long almacenamiento_1=1000000;
		long almacenamiento_2=2000000;
		long anchoBanda = 10000;
		
		List<Pe> listaCPU1 = new ArrayList<Pe>();
		listaCPU1.add(new Pe(0, new PeProvisionerSimple(mips_1)));
		listaCPU1.add(new Pe(1, new PeProvisionerSimple(mips_1)));
		
		
		
		List<Pe> listaCPU2 = new ArrayList<Pe>();
		listaCPU2.add(new Pe(0, new PeProvisionerSimple(mips_2)));
		listaCPU2.add(new Pe(1, new PeProvisionerSimple(mips_2)));
		listaCPU2.add(new Pe(2, new PeProvisionerSimple(mips_2)));
		listaCPU2.add(new Pe(3, new PeProvisionerSimple(mips_2)));
		
		Host[] host = new Host[Host_1 + Host_2];
		List<Host> listaHosts = new ArrayList<Host>();

		for (int i = 0; i < Host_1; i++) {
			host[i] = new Host(i, new RamProvisionerSimple(ram_1), new BwProvisionerSimple(anchoBanda),
					almacenamiento_1, listaCPU1, new VmSchedulerSpaceShared(listaCPU1));
			listaHosts.add(host[i]);
		}
		for (int i = Host_1; i < Host_2; i++) {
			host[i] = new Host(i, new RamProvisionerSimple(ram_2), new BwProvisionerSimple(anchoBanda),
					almacenamiento_2, listaCPU2, new VmSchedulerSpaceShared(listaCPU2));
			listaHosts.add(host[i]);
		}
		String arquitectura = "x86";
		String so = "Linux";
		String vmm = "Xen";
		String nombre = "Datacenter_0";
		double zonaHoraria = 1.0;
		double costePorSeg = 0.01;
		double costePorMem = 0.01;
		double costePorAlm = 0.01;
		double costePorBw = 0.01;
		DatacenterCharacteristics caracteristicas = new DatacenterCharacteristics(arquitectura, so, vmm, listaHosts,
				zonaHoraria, costePorSeg, costePorMem, costePorAlm, costePorBw);
		Datacenter centroDeDatos = null;
		try {
			centroDeDatos = new Datacenter(nombre, caracteristicas, new VmAllocationPolicySimple(listaHosts),
					new LinkedList<Storage>(), 0);
			//centroDeDatos = new Datacenter(nombre, caracteristicas, new VmAllocationPolicyRandom(listaHosts),
			//		new LinkedList<Storage>(), 0);
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

		switch (n) {
		case 1:
			for (int idx = 0; idx < 8; idx++) {
				virtualMachines.add(new Vm(virtualMachines.size(), broker.getId(), 2400, 1, 3072, 1000, 120, "Xen",
						new CloudletSchedulerSpaceShared()));
			}
			break;
		case 2:
			for (int idx = 0; idx < 16; idx++) {
				virtualMachines.add(new Vm(virtualMachines.size(), broker.getId(), 2000, 1, 2048, 1000, 80, "Xen",
						new CloudletSchedulerSpaceShared()));
			}
			break;
		case 3:
			for (int idx = 0; idx < 24; idx++) {
				virtualMachines.add(new Vm(virtualMachines.size(), broker.getId(), 1800, 1, 1024, 1000, 60, "Xen",
						new CloudletSchedulerSpaceShared()));
			}
			break;
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
