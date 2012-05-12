package aeminium.gpu.operations.random;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.bridj.Pointer;

import aeminium.gpu.devices.GPUDevice;
import aeminium.gpu.executables.GenericProgram;
import aeminium.gpu.templates.Template;
import aeminium.gpu.templates.TemplateWrapper;
import aeminium.gpu.utils.PathHelper;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;

public class MersenneTwisterGPU extends GenericProgram {

	private int PATH_N = 24000;
	private int MT_RNG_COUNT = 4096;
	private int N_PER_RNG = alignUp(divUp(PATH_N, MT_RNG_COUNT), 2);

	private static int sizeof_mt_struct_stripped = 4 * 4; // 4 * sizeof(int)

	protected int seed;
	private int size;

	CLKernel boxmuller;
	CLBuffer<?> parameters;
	CLBuffer<?> output, outputFinal;

	public MersenneTwisterGPU(GPUDevice dev, int size, int seed) {
		device = dev;
		this.seed = seed;
		this.size = size;
		MT_RNG_COUNT = size / N_PER_RNG;
	}

	@Override
	public void prepareBuffers(CLContext ctx) {
		Pointer<?> d = Pointer.allocateBytes(MT_RNG_COUNT
				* sizeof_mt_struct_stripped);
		d = d.order(ctx.getByteOrder());
		loadMTGPU(d, seed,
				PathHelper.openFileAsStream("data/MersenneTwister.dat"));
		parameters = ctx.createBuffer(CLMem.Usage.Input, d);
		output = ctx.createBuffer(Usage.Output,
				Pointer.allocateFloats(this.size));
	}

	@Override
	public void execute(CLContext ctx, CLQueue q) {
		synchronized (kernel) {
			// setArgs will throw an exception at runtime if the types / sizes
			// of the arguments are incorrect
			kernel.setArgs(output, parameters, N_PER_RNG);

			// Ask for 1-dimensional execution of length dataSize, with auto
			// choice of local workgroup size :
			kernelCompletion = kernel.enqueueNDRange(q,
					new int[] { MT_RNG_COUNT }, new CLEvent[] {});
		}
		boxmuller = getKernel(program, "BoxMuller");
		synchronized (boxmuller) {
			boxmuller.setArgs(output, N_PER_RNG);
			kernelCompletion = boxmuller.enqueueNDRange(q,
					new int[] { MT_RNG_COUNT },
					new CLEvent[] { kernelCompletion });
		}

	}

	@Override
	public void retrieveResults(CLContext ctx, CLQueue q) {
		outputFinal = output;
	}

	@Override
	public void release() {
		super.release();
		parameters.release();
		boxmuller.release();
	}

	private void loadMTGPU(Pointer<?> p, int seed, InputStream fis) {
		try {
			byte buffer[] = new byte[sizeof_mt_struct_stripped];
			int c = 0;
			for (int i = 0; i < MT_RNG_COUNT; i++) {
				fis.read(buffer);
				p.setBytesAtOffset(c, buffer);
				p.setIntAtOffset(c + buffer.length - 4, seed); // Replace last
																// parameter by
																// seed.
				c += buffer.length;
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				fis.close();
			} catch (IOException ex) {
			}
		}

	}

	@Override
	public String getSource() {
		Template t = new Template(new TemplateWrapper(
				"opencl/MersenneTwister.clt"));
		HashMap<String, String> d = new HashMap<String, String>();
		d.put("MT_RNG_COUNT", "" + MT_RNG_COUNT);
		return t.apply(d);
	}

	@Override
	public String getKernelName() {
		return "MersenneTwister";
	}

	// Helpers
	private static int divUp(int a, int b) {
		return ((a % b) != 0) ? (a / b + 1) : (a / b);
	}

	private static int alignUp(int a, int b) {
		return ((a % b) != 0) ? (a - a % b + b) : a;
	}

	@Override
	protected boolean willRunOnGPU() {
		return true;
	}

	@Override
	public void cpuExecution() {
		// Will never be called
		return;
	}

	public CLBuffer<?> getOutputBuffer() {
		return outputFinal;
	}
}
