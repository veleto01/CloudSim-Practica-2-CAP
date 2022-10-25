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

public class ejercicio5 {

	public ejercicio5() {
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

	// Por cada petición de máquina virtual, se escogerá aleatoriamente entre todos
	// los hosts del centro de datos

	// Si el host escogido no permite la creación de la máquina virtual, se repite
	// el proceso con otro host elegido
	// aleatoriamente. Este mecanismo se repite hasta encontrar un host válido o
	// hasta que se haya probado con
	// todos los hosts disponibles.

	// Si durante el proceso de selección de números aleatorios estos se repiten, no
	// se deberá probar nuevamente
	// con ese host (ya se sabe que no es posible crear ahí la máquina virtual).

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
			int idx=0;

			do {
				Random random = new Random();
				idx = random.nextInt(NUMERO_HOSTS-1 + min) ;
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
			centroDeDatos = new Datacenter(nombre, caracteristicas, new VmAllocationPolicyRandom(listaHosts),
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
