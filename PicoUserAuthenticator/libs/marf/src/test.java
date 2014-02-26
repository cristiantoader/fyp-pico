import marf.*;
import marf.Storage.*;

public class test
{
	public static void main(String[] argv)
	{
		MARF.setPreprocessingMethod(MARF.DUMMY);

		double[] data = {1, 2, 3};
		Sample oSample = new Sample(data);
	}
}
