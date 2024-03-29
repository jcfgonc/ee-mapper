package jcfgonc.eemapper.genetic;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import graph.GraphAlgorithms;
import graph.StringGraph;
import jcfgonc.eemapper.MappingAlgorithms;
import jcfgonc.eemapper.structures.MappingStructure;
import jcfgonc.genetic.operators.GeneticOperations;
import structures.OrderedPair;
import utils.VariousUtils;

public class MapperGeneticOperations implements GeneticOperations<MappingStructure<String, String>> {

	private static final double BRIDGE_JUMPING_RANGE = 3;
	private static final double LOCAL_JUMP_PROBABILITY = 0.88;
	private static final double MUTATION_JUMP_PROBABILITY_POWER = 2.5;
	private static final int DEEPNESS_LIMIT = Integer.MAX_VALUE;
	private StringGraph inputSpace;
	private ArrayList<String> vertexSetAsList;

	public MapperGeneticOperations(StringGraph inputSpace) {
		this.inputSpace = inputSpace;
		this.vertexSetAsList = new ArrayList<>(inputSpace.getVertexSet());
	}

	@Override
	public MappingStructure<String, String> createGeneCopy(MappingStructure<String, String> genes, boolean soonChanged) {
		MappingStructure<String, String> mappingStructure = new MappingStructure<String, String>();
		mappingStructure.setRefPair(new OrderedPair<>(genes.getRefPair())); // very important
		// shallow copy
		if (soonChanged) {
		} else { // deep copy
			mappingStructure.setPairGraph(genes.getPairGraph());
		}
		return mappingStructure;
	}

	@Override
	public void crossover(MappingStructure<String, String> parent0, MappingStructure<String, String> parent1, MappingStructure<String, String> offSpring0,
			MappingStructure<String, String> offSpring1, RandomGenerator random) {

	}

	@Override
	public double evaluateFitness(MappingStructure<String, String> mapStruct) {
		return mapStruct.getMapping().size();
	}

	@Override
	public MappingStructure<String, String> initializeGenes(RandomGenerator random) {
		MappingStructure<String, String> mappingStruct = new MappingStructure<>();
		// get a random concept pair
		OrderedPair<String> refPair = MappingAlgorithms.getRandomConceptPair(inputSpace, random);
		mappingStruct.setRefPair(refPair);

		MappingAlgorithms.updateMappingGraph(inputSpace, mappingStruct, DEEPNESS_LIMIT, random);
		return mappingStruct;
	}

	@Override
	public MappingStructure<String, String> mutateGenes(MappingStructure<String, String> genes, RandomGenerator random) {
		OrderedPair<String> refPair = genes.getRefPair();
		String leftElement = refPair.getLeftElement();
		String rightElement = refPair.getRightElement();

		// unalign/rearrange a new refpair from the existing
		// -------------------------------------------------
		// offset locally?
		if (random.nextDouble() < LOCAL_JUMP_PROBABILITY) {
			// do a random walk on either left or right concepts (or both)
			do {
				if (random.nextBoolean()) {
					double r = Math.pow(random.nextDouble(), MUTATION_JUMP_PROBABILITY_POWER);
					int hops = (int) Math.ceil(r * BRIDGE_JUMPING_RANGE);
					leftElement = GraphAlgorithms.getVertexFromRandomWalk(random, leftElement, inputSpace, hops);
				}
				if (random.nextBoolean()) {
					double r = Math.pow(random.nextDouble(), MUTATION_JUMP_PROBABILITY_POWER);
					int hops = (int) Math.ceil(r * BRIDGE_JUMPING_RANGE);
					rightElement = GraphAlgorithms.getVertexFromRandomWalk(random, rightElement, inputSpace, hops);
				}
			}
			// prevent left and right from being equals
			while (leftElement.equals(rightElement));
		}
		// offset globally
		else {
			// do a random shift to far away on either left or right concepts (or both)
			do {
				if (random.nextBoolean()) {
					leftElement = VariousUtils.getRandomElementFromCollection(vertexSetAsList, random);
				}
				if (random.nextBoolean()) {
					rightElement = VariousUtils.getRandomElementFromCollection(vertexSetAsList, random);
				}
			}
			// prevent left and right from being equals
			while (leftElement.equals(rightElement));
		}

		// if (refPair.getLeftElement().equals(leftElement) && //
		// refPair.getRightElement().equals(rightElement)) {
		// System.err.println("reference pair did not change");
		// }
		// store refpair back in the gene
		genes.setRefPair(new OrderedPair<String>(leftElement, rightElement));
		MappingAlgorithms.updateMappingGraph(inputSpace, genes, DEEPNESS_LIMIT, random);
		return genes;
	}

	@Override
	public MappingStructure<String, String> repairGenes(MappingStructure<String, String> genes, RandomGenerator random) {
		return genes;
	}

	@Override
	public boolean useCrossover() {
		return false;
	}

	@Override
	public boolean useGeneRepair() {
		return false;
	}

	@Override
	public boolean useMutation() {
		return true;
	}
}
