package marf.Classification.NeuralNetwork;

import marf.*;
import marf.Classification.*;
import marf.FeatureExtraction.*;
import marf.Storage.*;
import marf.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.io.*;
import java.util.*;

/**
 * Class NeuralNetwork
 * <p>$Header: /cvsroot/marf/marf/src/marf/Classification/NeuralNetwork/NeuralNetwork.java,v 1.27.2.1 2003/02/16 18:08:54 mokhov Exp $</p>
 * @author Ian Clement
 */
public class NeuralNetwork extends Classification
{
	/*
	 * ----------------------
	 * Enumeration
	 * ----------------------
	 */

	/**
	 * How many binary Neurons in the output layer.
	 * Presumably int (our ID) is 4 bytes, hence 4 * 8 = 32 bits, and so many outputs
	 */
	public static final int DEFAULT_OUTPUT_NEURON_BITS = 32;

	/**
	 * Default training constant of <code>1</code> if none supplied
	 */
	public static final double DEFAULT_TRAINING_CONSTANT = 1;

	/**
	 * Default number of epoch iterations of <code>64</code> if none supplied
	 */
	public static final int DEFAULT_EPOCH_NUMBER = 64;

	/**
	 * Default minimum training error of <code>0.1</code> if none supplied
	 */
	public static final double DEFAULT_MIN_ERROR = 0.1;


	/*
	 * ----------------------
	 * Data Members
	 * ----------------------
	 */

	/**
	 * Array of Layers
	 */
	private ArrayList layers = new ArrayList();

	/**
	 * Supposedly current layer. Ian?
	 */
	private ArrayList curr;

	/**
	 * Current layer's #
	 */
	private int currLayer = 0;

	/**
	 * currLayerBuf
	 */
	private int currLayerBuf = 0;

	/**
	 * Current Neuron
	 */
	private Neuron currNeuron;

	/**
	 * Neuron Type
	 */
	private int neuron_type = Neuron.UNDEF;

	/**
	 * Input layer
	 */
	private ArrayList inputs = new ArrayList();

	/**
	 * Output layer
	 */
	private ArrayList outputs = new ArrayList();

	/**
	 * All output will use this encoding
	 */
	static final String outputEncoding = "UTF-8";

	/* Constants used for JAXP 1.2 */

	/**
	 * JAXP 1.2 Schema
	 */
	static final String JAXP_SCHEMA_LANGUAGE =
		"http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	/**
	 * XML 2001 Schema
	 */
	static final String W3C_XML_SCHEMA =
		"http://www.w3.org/2001/XMLSchema";

	/**
	 * JAXP 1.2 Schem URL
	 */
	static final String JAXP_SCHEMA_SOURCE =
		"http://java.sun.com/xml/jaxp/properties/schemaSource";


	/*
	 * ----------------------
	 * Methods
	 * ----------------------
	 */

	/**
	 * NeuralNetwork Constructor
	 * @param poFeatureExtraction FeatureExtraction module reference
	 */
	public NeuralNetwork(FeatureExtraction poFeatureExtraction)
	{
		super(poFeatureExtraction);
	}

	/* Classification API */

	/**
	 * Implementes training of Neural Net
	 * @return <code>true</code>
	 * @exception ClassificationException
	 */
	public final boolean train() throws ClassificationException
	{
		Vector oTrainingSamples = null;
		Vector oParams = null;

		try
		{
			/*
			 * Get newly coming feature vector into the TrainingSet cluster
			 * in case it's not there.
			 */
			super.train();

			// Defaults
			double trainconst = DEFAULT_TRAINING_CONSTANT;
			int    epochNum   = DEFAULT_EPOCH_NUMBER;
			double minErr     = DEFAULT_MIN_ERROR;

			// Defaults can be overriden by an app
			if(MARF.getModuleParams() != null)
			{
				oParams = MARF.getModuleParams().getClassificationParams();

				if(oParams.size() > 1)
				{
					// May throw NullPointerException
					trainconst = ((Double)oParams.elementAt(1)).doubleValue();
					epochNum   = ((Integer)oParams.elementAt(2)).intValue();
					minErr     = ((Double)oParams.elementAt(3)).doubleValue();
				}
			}

			restore();

			// Get the Training set...
			oTrainingSamples = this.oTrainingSet.getTrainingSamples();

			// Set initial values to always enter the epoch training loop
			int    limit = 0;
			double error = minErr + 1;

			// Epoch training
			while(error > minErr && limit < epochNum)
			{
				// Execute the training for each training utterance
				for(int i = 0; i < oTrainingSamples.size(); i++)
				{
					TrainingSet.TrainingSample samp = (TrainingSet.TrainingSample)oTrainingSamples.get(i);
					train(samp.getMeanVector(), samp.getSubjectID(), trainconst);

					// Commit modified weight
					commit();
				}

				// Test new values and calc error... Testing is done with the same training samples :-(
				int count = 0;
				error = 0.0;

				for(count = 0; count < oTrainingSamples.size(); count++)
				{
					TrainingSet.TrainingSample samp = (TrainingSet.TrainingSample)oTrainingSamples.get(count);

					setInputs(samp.getMeanVector());
					runNNet();

					int res = interpretAsBinary();

					error += Math.abs(samp.getSubjectID() - res);

					MARF.debug("Expected: " + samp.getSubjectID() + " Got: " + res + " Error: " + error);
				}

				if(count == 0)
					throw new ClassificationException("NeuralNetwork.train() --- There are no training samples!");

				error /= (double)count;
				limit++;

				MARF.debug("Epoch: error = " + error + ", limit = " + limit);
			}

			dump();

			return true;
		}
		catch(IOException e)
		{
			throw new ClassificationException
			(
				"IOException while dumping/restoring neural net: " +
				e.getMessage()
			);
		}
		catch(NullPointerException e)
		{
			throw new ClassificationException
			(
				"NeuralNetwork.train(): Missing required ModuleParam (" + oParams +
				") or TrainingSample (" + oTrainingSamples + ")"
			);
		}
	}

	/**
	 * Neural Network implementation of classification routine
	 * @return <code>true</code>
	 * @exception ClassificationException
	 */
	public final boolean classify() throws ClassificationException
	{
		try
		{
			double[] adFeatures = this.oFeatureExtraction.getFeaturesArray();

			restore();

			if(adFeatures.length != inputs.size())
				throw new ClassificationException
				(
					"Input array size (" + adFeatures.length + ") not consistent with input layer (" +
					inputs.size() + ")"
				);

			for(int i = 0; i < adFeatures.length; i++)
				((Neuron)inputs.get(i)).result = adFeatures[i];

			runNNet();

			// Make result...
			int res = interpretAsBinary();

			// TODO: second closest
			this.oResult = new Result(res);

			return true;
		}
		catch(IOException e)
		{
			throw new ClassificationException(e.getMessage());
		}
	}

	/**
	 * Evaluates the entire neural network
	 */
	private final void runNNet()
	{
		ArrayList current = null;

		for(int i = 0; i < layers.size(); i++)
		{
			current = (ArrayList)layers.get(i);

			for(int j = 0; j < current.size(); j++)
				((Neuron)current.get(j)).eval();
		}
	}

	//----------- Methods for Creating the NNet -----------------

	/**
	 * Parses XML and produces NNet.
	 * @exception IOException
	 */
	public final void initialize(final String filename, final boolean dtd) throws java.io.IOException
	{
		try
		{
			MARF.debug("Initializing XML parser...");

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			dbf.setNamespaceAware(true);
			dbf.setValidating(dtd);

			DocumentBuilder db = dbf.newDocumentBuilder();
			OutputStreamWriter errorWriter = new OutputStreamWriter(System.err, outputEncoding);

			db.setErrorHandler(new MyErrorHandler(new PrintWriter(errorWriter, true)));

			MARF.debug("Parsing XML file...");
			Document doc = db.parse(new File(filename));

			//Add input layer
			layers.add(inputs);

			//Build NNet structure
			MARF.debug("Making the NNet structure...");
			buildNNet(doc);

			//add output layer
			layers.add(outputs);

			//fix inputs/outputs
			MARF.debug("Setting the inputs and outputs for each Neuron...");
			currLayer = 0;
			createLinks(doc);
		}
		catch(FileNotFoundException e)
		{
			try
			{
				MARF.debug("Generating new net...");

				int iFeaturesNum = this.oFeatureExtraction.getFeaturesArray().length;

				int iLastHiddenNeurons = Math.abs(iFeaturesNum - DEFAULT_OUTPUT_NEURON_BITS) / 2;

				if(iLastHiddenNeurons == 0)
					iLastHiddenNeurons = iFeaturesNum / 2;

				// Generate fully linked 3-layer neural net with random weights
				generate
				(
					// As many inputs as features
					iFeaturesNum,

					// "Middleware", from air
					new int[]
					{
						iFeaturesNum * 2,
						iFeaturesNum,
						iLastHiddenNeurons
					},

					// Output layer
					DEFAULT_OUTPUT_NEURON_BITS
				);

				MARF.debug("Dumping newly generated net...");
				dump();
			}
			catch(ClassificationException oClassificationException)
			{
				throw new IOException(oClassificationException.getMessage());
			}
		}
		catch(Exception e)
		{
			throw new IOException(e.getMessage());
		}
    }

	/**
	 * DOM tree traversal -- build NNet structure
	 * @param n current root Node
	 */
	private final void buildNNet(Node n)
	{
		String name, value;
		int type = n.getNodeType();

		if(type == Node.ELEMENT_NODE)
		{
			name = n.getNodeName();

			//not yet ;-)
			if(name.equals("input") || name.equals("output"))
				return;

			//System.out.println("Making " + name + "...");

			NamedNodeMap atts = n.getAttributes();

			if(name.equals("layer"))
			{
				for(int i = 0; i < atts.getLength(); i++)
				{
					Node att = atts.item(i);

					String attName = att.getNodeName();
					String attValue = att.getNodeValue();

					if(attName.equals("type"))
					{
						if(attValue.equals("input"))
						{
							curr = inputs;
							neuron_type = Neuron.INPUT;
						}
						else if(attValue.equals("output"))
						{
							curr = outputs;
							neuron_type = Neuron.OUTPUT;
						}
						else
						{
							curr = new ArrayList();
							layers.add(curr);
							neuron_type = Neuron.HIDDEN;
						}
					}
					else if(attName.equals("index"))
					{
						MARF.debug("Indexing layers currently not supported... Assumes written order");
					}
					else
					{
						System.out.println("Unknown layer attribute: " + attName);
					}
				}
			}
			else if(name.equals("neuron"))
			{
				String neuron_name = new String();
				double thresh = 0.0;

				for(int i = 0; i < atts.getLength(); i++)
				{
					Node att = atts.item(i);

					String attName = att.getNodeName();
					String attValue = att.getNodeValue();

					if(attName.equals("index"))
					{
						//System.out.println("Setting neuron name to " + attValue);
						neuron_name = new String(attValue);
					}
					else if(attName.equals("thresh"))
					{
						try
						{
							thresh = Double.valueOf(attValue.trim()).doubleValue();
							//System.out.println("Setting threshold to " + thresh + ".");
						}
						catch(NumberFormatException nfe)
						{
							System.out.println("NumberFormatException: " + nfe.getMessage());
						}
					}
					else
					{
						System.out.println("Unknown layer attribute: " + attName);
					}
				}

				//System.out.println("Making new neuron " + neuron_name + " of type " + type);

				Neuron tmp = new Neuron(neuron_name, neuron_type);
				tmp.threshold = thresh;
				curr.add(tmp);
			}
		}

		// Recurse for children if any
		for(Node child = n.getFirstChild(); child != null; child = child.getNextSibling())
			buildNNet(child);
	}

	/**
	 * DOM tree traversal -- create input and output links
	 * @param n Node
	 * @exception ClassificationException
	 */
	private final void createLinks(Node n) throws ClassificationException
	{
		int type = n.getNodeType();
		String name, value;

		if(type == Node.ELEMENT_NODE)
		{
			name = n.getNodeName();

			NamedNodeMap atts = n.getAttributes();

			if(name.equals("layer"))
			{
				for(int i = 0; i < atts.getLength(); i++)
				{
					Node att = atts.item(i);

					String attName = att.getNodeName();
					String attValue = att.getNodeValue();

					if(attName.equals("type"))
					{
						if(attValue.equals("input"))
						{
							curr = inputs;
							currLayer = 0;
						}
						else if(attValue.equals("output"))
						{
							curr = outputs;
							currLayer = layers.size() - 1;
						}
						else
						{
							currLayer = ++currLayerBuf;
							curr = (ArrayList)layers.get(currLayer);
						}

						//System.out.println("Moving to layer " + currLayer + " [currLayerBuf is " + currLayerBuf + "]");
					}
				}
			}
			else if(name.equals("neuron"))
			{
				String index = new String();

				for(int i = 0; i < atts.getLength(); i++)
				{
					Node att = atts.item(i);

					String attName = att.getNodeName();
					String attValue = att.getNodeValue();

					if(attName.equals("index"))
						index = new String(attValue);
				}

				currNeuron = getNeuron(curr, index);

			}
			else if(name.equals("input"))
			{
				String index = null;
				double weight = -1.0;

				for(int i = 0; i < atts.getLength(); i++)
				{
					Node att = atts.item(i);

					String attName = att.getNodeName();
					String attValue = att.getNodeValue();

					if(attName.equals("ref"))
						index = new String(attValue);
					else if(attName.equals("weight"))
					{
						try
						{
							weight = Double.valueOf(attValue.trim()).doubleValue();
						}
						catch(NumberFormatException nfe)
						{
							System.out.println("NumberFormatException: " + nfe.getMessage());
						}
					}
				}

				//if(weight < 0.0) {
				//    throw new ClassificationException("Bad \'weight\' defined for neuron " + currNeuron.name + " in layer " + currLayer);
				//}

				if(index == null || index.equals(""))
					throw new ClassificationException
					(
						"No 'ref' value assigned for neuron " +
						currNeuron.name +
						" in layer " + currLayer
					);

				//System.out.println("Adding input " + index + " with weight " + weight);

				if(currLayer > 0)
				{
					Neuron toAdd = getNeuron((ArrayList)layers.get(currLayer - 1), index);

					if(toAdd == null)
						throw new ClassificationException("Cannot find neuron " + index + " in layer " + (currLayer - 1));

					currNeuron.addInput(toAdd, weight);
				}
				else
					throw new ClassificationException("Input element not allowed in input layer");

			}

			else if(name.equals("output"))
			{
				String index = null;

				for(int i = 0; i < atts.getLength(); i++)
				{
					Node att = atts.item(i);

					String attName = att.getNodeName();
					String attValue = att.getNodeValue();

					if(attName.equals("ref"))
						index = new String(attValue);
				}

				if(index == null || index.equals(""))
					throw new ClassificationException
					(
						"No 'ref' value assigned for neuron " + currNeuron.name +
						" in layer " + currLayer
					);

				//System.out.println("Adding output " + index);

				if(currLayer >= 0)
				{
					Neuron toAdd = getNeuron((ArrayList)layers.get(currLayer + 1), index);

					if(toAdd == null)
						throw new ClassificationException("Cannot find neuron " + index + " in layer " + (currLayer + 1));

					currNeuron.addOutput(toAdd);
				}

			}
		}

		// Recurse for children if any
		for(Node child = n.getFirstChild(); child != null; child = child.getNextSibling())
			createLinks(child);
    }

    //----------- Methods for Running the NNet -----------------

	/**
	 * Sets inputs
	 * @param in double array of input features
	 * @exception ClassificationException
	 */
	private final void setInputs(final double[] in) throws ClassificationException
	{
		if(in.length != inputs.size())
			throw new ClassificationException("Input array size not consistent with input layer.");

		for(int i = 0; i < in.length; i++)
			((Neuron)inputs.get(i)).result = in[i];
	}

	/**
	 * Gets outputs of a neural network run
	 * @return array of doubles
	 */
	public double[] getRes()
	{
		double[] ret = new double[outputs.size()];

		for(int i = 0; i < outputs.size(); i++)
			ret[i] = ((Neuron)outputs.get(i)).result;

		return ret;
	}

    //----------- Methods for Outputting the NNet -----------------

	/**
	 * Indents the output according to the requested tabulation.
	 * For pretty indentation
	 * @param bw Writer object
	 * @param n number of tabs
	 * @exception IOException
	 */
	public static final void indent(BufferedWriter bw, final int n) throws java.io.IOException
	{
		for(int i = 0; i < n; i++)
			bw.write("\t");
	}

	/**
	 * Dumps Neural Network as XML file
	 * @param filename XML file name to write to
	 * @exception IOException
	 */
	public final void dumpXML(final String filename) throws java.io.IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

		bw.write("<?xml version=\"1.0\"?>");
		bw.newLine();
		bw.write("<net>");
		bw.newLine();

		for(int i = 0; i < layers.size(); i++)
		{
			ArrayList tmp_layer = (ArrayList)layers.get(i);

			indent(bw, 1);
			bw.write("<layer type=\"");

			if(i == 0)
				bw.write("input");
			else if(i == layers.size() - 1)
				bw.write("output");
			else
				bw.write("hidden");

			bw.write("\" index=\"" + i + "\">");
			bw.newLine();

			for(int j = 0; j < tmp_layer.size(); j++)
			{
				Neuron tmp_neuron = (Neuron)tmp_layer.get(j);
				tmp_neuron.printXML(bw, 2);
			}

			indent(bw, 1);
			bw.write("</layer>");
			bw.newLine();
		}

		bw.write("</net>");
		bw.newLine();

		bw.close();
	}

	/**
	 * Generates virgin net at random.
	 * @param piNumOfInputs number of input Neurons in the input layer
	 * @param paiHiddenLayers arrays of numbers of Neurons in the hidden layers
	 * @param piNumOfOutputs number of output Neurons in the output layer
	 * @since 0.2.0, Serguei
	 * @exception ClassificationException
	 */
	public final void generate(int piNumOfInputs, int[] paiHiddenLayers, int piNumOfOutputs) throws ClassificationException
	{
		if(paiHiddenLayers == null || paiHiddenLayers.length == 0)
			throw new ClassificationException("Number of hidded layers may not be null or of 0 length.");

		for(int i = 1; i <= 1 + paiHiddenLayers.length + 1; i++)
		{
			// Add input layer
			if(i == 1)
			{
  				for(int j = 1; j <= piNumOfInputs; j++)
  				{
					Neuron oFreshAndJuicyNeuron = new Neuron("" + j, Neuron.INPUT);
					oFreshAndJuicyNeuron.threshold = 1.0;
					inputs.add(oFreshAndJuicyNeuron);
				}

				layers.add(inputs);

				continue;
			}

			// Add output layer
			if(i == 1 + paiHiddenLayers.length + 1)
			{
  				for(int j = 1; j <= piNumOfOutputs; j++)
  				{
					Neuron oFreshAndJuicyNeuron = new Neuron("" + j, Neuron.OUTPUT);
					oFreshAndJuicyNeuron.threshold = 1.0;
					outputs.add(oFreshAndJuicyNeuron);
				}

				layers.add(outputs);

				continue;
			}

			ArrayList oHiddenLayer = new ArrayList();

			// Add hidden layers
			for(int j = 1; j <= paiHiddenLayers[i - 2]; j++)
			{
				Neuron oFreshAndJuicyNeuron = new Neuron("" + j, Neuron.HIDDEN);
				oFreshAndJuicyNeuron.threshold = 1.0;
				oHiddenLayer.add(oFreshAndJuicyNeuron);
			}

			layers.add(oHiddenLayer);
		}

		// Fix inputs / outputs
		MARF.debug("Setting the inputs and outputs for each Neuron...");

		for(int iCurrentLayer = 0; iCurrentLayer < layers.size() - 1; iCurrentLayer++)
		{
			ArrayList oCurrentLayer = (ArrayList)layers.get(iCurrentLayer);

			for(int n = 0; n < oCurrentLayer.size(); n++)
			{
				Neuron oCurrentNeuron = (Neuron)oCurrentLayer.get(n);

				ArrayList oNextLayer = (ArrayList)layers.get(iCurrentLayer + 1);

				for(int k = 0; k < oNextLayer.size(); k++)
				{
					Neuron oNextLayerNeuron = (Neuron)oNextLayer.get(k);
					oCurrentNeuron.addOutput(oNextLayerNeuron);
					oNextLayerNeuron.addInput(oCurrentNeuron, new Random().nextDouble() * 2.0 - 1.0);
				}
			}
		}
	}


	/**
	 * Returns the Neuron called by String name.
	 * @param a ArrayList
	 * @param name String
	 * @return Neuron object
	 */
	private Neuron getNeuron(ArrayList a, final String name)
	{
		Neuron ret = null;

		for(int i = 0; i < a.size(); i++)
		{
			ret = (Neuron)a.get(i);

			if(ret.name.equals(name))
				break;
		}

		return ret;
	}

    //----------- Method for Training the NNet -----------------

	/**
	 * Actual training of the net
	 * @param in double[]
	 * @param expected int
	 * @param trainconst double
	 * @exception ClassificationException
	 */
	public final void train(final double[] in, int expected, final double trainconst) throws ClassificationException
	{
		if(trainconst <= 0.0)
			throw new ClassificationException
			(
				"NeuralNetwork.train(): Training constant must be >= 0.0, supplied: " +
				trainconst
			);

		if(in.length != inputs.size())
			throw new ClassificationException
			(
				"NeuralNetwork.train(): Input array size (" + in.length +
				") not consistent with input layer (" + inputs.size() + ")"
			);

		/*
		 * Setup NNet with training data.
		 */

		// Must setup the input data...
		setInputs(in);

		//if(expected/*.length*/ != outputs.size())
		//    throw new ClassificationException("Expected array size not consistent with output layer.");

		// Run on training data. Must fix...
		runNNet();

		for(int k = outputs.size() - 1; k >= 0; k--)
		{
			int currEx = expected % 2;
			expected /= 2;
			((Neuron)outputs.get(k)).train((double)currEx, trainconst, 1.0);
		}

		for(int i = layers.size() - 2; i >= 0; i--)
		{
			ArrayList tmp = (ArrayList)layers.get(i);

			for(int j = 0; j < tmp.size(); j++)
				((Neuron)tmp.get(j)).train(0.0, trainconst, 1.0);
		}
	}

	/**
	 * Applies changes made to neurons on every net's layer
	 */
	public final void commit()
	{
		for(int i = 0; i < layers.size(); i++)
		{
			ArrayList tmp = (ArrayList)layers.get(i);

			for(int j = 0; j < tmp.size(); j++)
				((Neuron)tmp.get(j)).commit();
		}
	}

	/**
	 * Interprets net's binary output as an ID for the finale classification result
	 * @return ID, integer
	 */
	private final int interpretAsBinary()
	{
		int ret = 0;

		for(int i = 0; i < outputs.size(); i++)
		{
			ret *= 2; //binary displacement happens to not have effect first iteration :-P

			double tmp = ((Neuron)outputs.get(i)).result;

			if(tmp > 0.5)
				ret += 1;

			MARF.debug(((Neuron)outputs.get(i)).result + ",");
		}

		MARF.debug("Interpreted binary result = " + ret);

		return ret;
	}

    /* From Storage Manager */

	/**
	 * Dumps Neural Net to an XML file
	 * @exception IOException
	 */
	public void dump() throws java.io.IOException
	{
		dumpXML
		(
			this.getClass().getName() + "." +
			MARF.getPreprocessingMethod() + "." +
			MARF.getFeatureExtractionMethod() + ".xml"
		);
	}

	/**
	 * Restores Neural Net from an XML file
	 * @exception IOException
	 */
	public void restore() throws java.io.IOException
	{
		initialize
		(
			this.getClass().getName() + "." +
			MARF.getPreprocessingMethod() + "." +
			MARF.getFeatureExtractionMethod() + ".xml",

			false
		);
	}

	//iclement: This may need revision:
	//mokhov: i guess so

	// Error handler to report errors and warnings
	private static class MyErrorHandler implements ErrorHandler
	{
		/** Error handler output goes here */
		private PrintWriter out;

		MyErrorHandler(PrintWriter out)
		{
			this.out = out;
		}

		/**
		 * Returns a string describing parse exception details
		 */
		private String getParseExceptionInfo(SAXParseException spe)
		{
			String systemId = spe.getSystemId();

			if(systemId == null)
				systemId = "null";

			String info =
				"URI=" + systemId +
				" Line=" + spe.getLineNumber() +
				": " + spe.getMessage();

			return info;
		}

		// The following methods are standard SAX ErrorHandler methods.
		// See SAX documentation for more info.

		public void warning(SAXParseException spe) throws SAXException
		{
			out.println("Warning: " + getParseExceptionInfo(spe));
		}

		public void error(SAXParseException spe) throws SAXException
		{
			String message = "Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}

		public void fatalError(SAXParseException spe) throws SAXException
		{
			String message = "Fatal Error: " + getParseExceptionInfo(spe);
			throw new SAXException(message);
		}
	}
}

// EOF
