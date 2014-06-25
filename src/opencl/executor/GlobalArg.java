package opencl.executor;

public class GlobalArg extends KernelArg {
    public static native GlobalArg createInput(float[] array);
    public static native GlobalArg createOutput(int size);

    GlobalArg(long handle) {
        super(handle);
    }

    public native float at(int index);
}
