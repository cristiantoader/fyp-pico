package marf.Classification.NeuralNetwork;

import java.util.*;
import java.io.*;

/**
 * Class Neuron
 * <p>A basic element of a neural network</p>
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/NeuralNetwork/Neuron.java,v 1.6.2.1 2003/02/16 18:08:54 mokhov Exp $</p>
 */
public class Neuron
{
	/*
	 * ----------------------
	 * Enumeration
	 * ----------------------
	 */

	/**
	 * Indicates undefined neuron type
	 */
	public static final int UNDEF  = -1;

	/**
	 * Indicates input neuron
	 */
	public static final int INPUT  = 0;

	/**
	 * Indicates middle (hidden) neuron
	 */
	public static final int HIDDEN = 1;

	/**
	 * Indicates output neuron
	 */
	public static final int OUTPUT = 2;


	/*
	 * ----------------------
	 * Data Members
	 * ----------------------
	 */

	/**
	 * Neuron's name
	 */
	public String name;

	/**
	 * Current neuron type
	 */
	public int type = UNDEF;

	/**
	 * Inputs of the current Neuron
	 */
	private ArrayList inputs = new ArrayList();

	/**
	 * Inputs' weights
	 */
	private ArrayList weights = new ArrayList();

	/**
	 * Buffered weights to be comitted
	 */
	private ArrayList weightsBuf = new ArrayList();

	/**
	 * Outputs of the current Neuron
	 */
	private ArrayList outputs = new ArrayList();

	/**
	 * Used in error calculation
	 */
	public double delta = 0.0;

	/**
	 * Activation threshhold
	 */
	public double threshold = 0.0;

	/**
	 * Current Neuron's result
	 */
	public double result = 0.0;


	/*
	 * ----------------------
	 * Methods
	 * ----------------------
	 */

	/**
	 * Neuron Constructor
	 * @param n Neuron's name
	 * @param t Neuron's type
	 */
	public Neuron(String n, int t)
	{
		name = new String(n);
		type = t;
	}

	/**
	 * Adds an input neuron and its associated weight
	 * @param in Neuron to be added
	 * @param weight associated weight
	 * @return <code>true</code> if add was successful
	 */
	public final boolean addInput(Neuron in, double weight)
	{
		// XXX: Ian, did you really mean '&&' and not '&' in here?
		// because if inputs.add(in) returns false,
		// other parts don't get executed at all
/*		return
			inputs.add(in) &&
			weights.add(new Double(weight)) &&
			weightsBuf.add(new Double(weight));
*/
		return
			inputs.add(in) &
			weights.add(new Double(weight)) &
			weightsBuf.add(new Double(weight));

	}

	/**
	 * Adds an output neuron
	 * @param out the Neuron to add
	 * @return true if the neuron was added
	 */
	public final boolean addOutput(Neuron out)
	{
		return outputs.add(out);
	}

	/**
	 * Evaluates current neuron's value
	 */
	public final void eval()
	{
		if(type == INPUT) return; //assumes that the result of an input neuron is == the input

		if(inputs.isEmpty()) return;

		double count = 0;

		for(int i = 0; i < inputs.size(); i++)
			count += ((Neuron)inputs.get(i)).result * ((Double)weights.get(i)).doubleValue();

		count -= threshold;

		result = 1.0 / (1.0 + Math.exp(-count));

		//System.out.println("Neuron: " + name + ", Sum: " + count + ", Result: " + result);
	}

	/**
	 * Retrieves specific neuron's weight
	 * @param n Neuron to work on
	 * @return weight (double)
	 */
	private final double getWeight(Neuron n)
	{
		int val = inputs.indexOf(n);

		if(val >= 0)
			return ((Double)weights.get(val)).doubleValue();

		//System.out.println("There is no pointer n in neuron");

		return -1.0;
	}

	/**
	 * Neuron training
	 * @param expected expected value
	 * @param alpha used in error calculation
	 * @param beta used in error calculation
	 */
	public final void train(final double expected, final double alpha, final double beta)
	{
		switch(type)
		{
			case OUTPUT: //output nodes calc delta differntly based on expected res...
				this.delta = (expected - result) * result * (1.0 - result);
				break;

			case HIDDEN:
			{
				double sum = 0.0;

				for(int i = 0; i < outputs.size(); i++)
					sum += ((Neuron)outputs.get(i)).delta * ((Neuron)outputs.get(i)).getWeight(this);

				this.delta = result * (1.0 - result) * sum;
				break;
			}

			// No need to train INPUT-type neurons
			case INPUT:
			default:
				return;
		}

		//System.out.println("Neuron: " + name + ", Delta: " + delta);

		// Buffer the new weights to commit them later
		for(int i = 0; i < inputs.size(); i++)
		{
			this.weightsBuf.set
			(
				i,
				new Double(beta * ((Double)weights.get(i)).doubleValue() + alpha * this.delta * ((Neuron)inputs.get(i)).result)
			);

			//double arg = ((Double)weights.get(i)).doubleValue();
			//System.out.println("\tNew weight " + i + ": " + arg);
		}
	}

	/**
	 * Applies weight changes
	 */
	public final void commit()
	{
		for(int i = 0; i < weights.size(); i++)
			weights.set(i, new Double(((Double)weightsBuf.get(i)).doubleValue()));
	}

	/**
	 * Dumps XML of the current Neuron
	 * @param bw Writer object to write output
	 * @param tab tabulation needed
	 */
	public final void printXML(BufferedWriter bw, final int tab) throws java.io.IOException
	{
		NeuralNetwork.indent(bw, tab);

		bw.write("<neuron index=\"" + name + "\" thresh=\"" + threshold + "\">");
		bw.newLine();

		for(int i = 0; i < inputs.size(); i++)
		{
			NeuralNetwork.indent(bw, tab + 1);
			bw.write("<input ref=\"" + ((Neuron)inputs.get(i)).name + "\" weight=\"" + (Double)weights.get(i) + "\"/>");
			bw.newLine();
		}

		for(int i = 0; i < outputs.size(); i++)
		{
			NeuralNetwork.indent(bw, tab + 1);
			bw.write("<output ref=\"" + ((Neuron)outputs.get(i)).name + "\"/>");
			bw.newLine();
		}

		NeuralNetwork.indent(bw, tab);
		bw.write("</neuron>");
		bw.newLine();
	}
}

// EOF
