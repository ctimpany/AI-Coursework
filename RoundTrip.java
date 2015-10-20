import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


public class RoundTrip 
{
	
	private static String[] cities;
	private static int[][] distance;
	private static String originCity = "Edinburgh";
	
	private static String[][] population;
	private static String[][] intermediatePopulation;
	private static int bestRoute;
	private static double bestFitness = 0;
	
	private static int generation = 1;
	private final static int MAX_POPULATION = 10;
	private final static int MAX_GENERATIONS = 20;
	private final static double MUTATION_RATE = 0.2; //ratio of how many genes within a child will be swapped. between 0 and 1.
	private final static double CROSSOVER_RATE = 0.2; //ratio over which genes from each parent will be distributed within the child. Between 0 and 1.
	private static Random random = new Random();
	
	private static double[][] rouletteWheel = new double[MAX_POPULATION][2];
	private static double[] routeFitness = new double[MAX_POPULATION];
	private static int[] routeDistance = new int[MAX_POPULATION];
	
	/*
	 * This method takes in the file containing the distance matrix and converts it into 2 arrays.
	 * 1 containing the cities, and 1 containing the distances.
	 */
	private static void createMatrices() throws IOException
	{
			BufferedReader reader = new BufferedReader(new FileReader("../RoundTrip/src/Cities.txt"));
			String strIn = reader.readLine();
			cities = strIn.split("\t");
			String[] tmp;
			distance = new int[cities.length][cities.length];
			for(int i = 0; i<cities.length; i++)
			{
				strIn = reader.readLine();
				tmp = strIn.split("\t");
				for(int j = 0; j<cities.length; j++)
				{
					distance[i][j] = Integer.parseInt(tmp[j]);
				}
			}
	}
	
	/*
	 * This method creates the initial population for the first generation.
	 */
	private static void initialisePopulation()
	{
		int cityIndex;
		for(int i = 0; i<MAX_POPULATION;i++)
		{
			String[] tmpCities = new String [cities.length];
			System.arraycopy(cities, 0, tmpCities, 0, cities.length);
			
			population[i][0] = originCity;
			
			population[i][cities.length] = originCity;
			
			for(int j = 1; j<cities.length; j++)
			{
				if(population[i][j]==null)
				{
					cityIndex = random.nextInt(cities.length);
					while(tmpCities[cityIndex]==null || tmpCities[cityIndex].equals(originCity))
					{
						cityIndex = random.nextInt(cities.length);
					}
					population[i][j] = tmpCities[cityIndex];
					tmpCities[cityIndex]=null;
				}
				
			}
		}
	}
	
	/*
	 * This method finds the distance between 2 cities.
	 */
	private static String calculateDistance(String to, String from)
	{
		int distanceValue;
		int toIndex = -1;
		int fromIndex = -1;
		for(int i = 0; i<cities.length; i++)
		{
			if(cities[i].equals(to))
			{
				toIndex=i;
			}else if(cities[i].equals(from))
			{
				fromIndex=i;
			}
		}
		
		distanceValue = distance[fromIndex][toIndex];
		return Integer.toString(distanceValue);
	}
	
	/*
	 * This method takes in a cities name and uses it to find it's associated index in the cities array.
	 */
	private static int findCityIndex(String city)
	{
		int cityIndex = -1;
		for(int i = 0; i<cities.length; i++)
		{
			if(cities[i].equals(city))
			{
				cityIndex=i;
			}
		}
		
		return cityIndex;
	}
	
	/*
	 * This method evaluates each member of the population based on route distance and route fitness.
	 */
	private static void evaluatePopulation()
	{
		bestFitness = 0;
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			int totalDistance = 0;
			for(int j = 0; j<cities.length; j++)
			{
				totalDistance += Integer.parseInt(calculateDistance(population[i][j+1], population[i][j]));
				
			}
			routeDistance[i] = totalDistance;
			routeFitness[i] = 1000.0 / totalDistance;
			
			if(routeFitness[i] > bestFitness)
			{
				bestFitness = routeFitness[i];
				bestRoute = i;
			}
		}
	}
	
	/*
	 * This method replaces any null values in the intermediate population with the first non-null entry from the cityCheckList array.
	 * null entries in the intermediate population are from when the same city has tried to be entered twice in the route (ignoring the origin city).
	 * null entries in the cityCheckList array are created when a city has been entered into the route. i.e any city in the route is removed from the cityCheckList leaving only cities which have not been entered.
	 */
	private static void replaceRepeats(int populationPosition, String[] cityCheckList)
	{
		for(int i = 1; i<cities.length; i++)
		{
			if(intermediatePopulation[populationPosition][i] == null)
			{
				for(int j = 0; j<cities.length; j++)
				{
					if(!(cityCheckList[j] == null))
					{
						intermediatePopulation[populationPosition][i] = cityCheckList[j];
						cityCheckList[j] = null;
					}
				}
			}
		}
	}
	
	/*
	 * This method creates a new generation using members of the previous generation as 'parents' to the members of the new generation.
	 * The first member of the new generation is the 'best solution' from the previous generation.
	 */
	private static void generateNewGeneration()
	{
		for(int i = 0; i<cities.length+1; i++)
		{
			intermediatePopulation[0][i] = population[bestRoute][i];
		}
		
		int parent1, parent2, ratio, ratioCount;
		DecimalFormat df = new DecimalFormat("#");
		String[] tmpCities;
		
		ratio = Integer.parseInt(df.format((cities.length)*CROSSOVER_RATE));
		
		for(int i = 1; i<population.length; i++)
		{
			parent1 = rouletteWheel();
			parent2 = rouletteWheel();
			
			tmpCities = new String [cities.length];
			System.arraycopy(cities, 0, tmpCities, 0, cities.length);

			intermediatePopulation[i][0] = originCity;
			
			intermediatePopulation[i][cities.length] = originCity;
			tmpCities[findCityIndex(originCity)] = null;
			
			ratioCount = 0;
			
			for(int j = 1; j<cities.length; j++)
			{
				if(intermediatePopulation[i][j]==null)
				{
					if(ratioCount<ratio && tmpCities[findCityIndex(population[parent1][j])] != null)
					{
						intermediatePopulation[i][j] = population[parent1][j];
						tmpCities[findCityIndex(population[parent1][j])] = null;
						ratioCount++;
					}else if(tmpCities[findCityIndex(population[parent2][j])] != null)
					{
						intermediatePopulation[i][j] = population[parent2][j];
						tmpCities[findCityIndex(population[parent2][j])] = null;
					}
				}				
			}	
			replaceRepeats(i, tmpCities);
			childMutation(i);
		}
		population = intermediatePopulation;
		
	}
	
	/*
	 * This method swaps a number of genes in the child based on the mutation rate specified.
	 */
	private static void childMutation(int placeInPopulation)
	{
		DecimalFormat df = new DecimalFormat("#");
		int ratio = Integer.parseInt(df.format((cities.length+1)*MUTATION_RATE));
		int randInt;
		String tmpCity;
		
		for(int i = 0; i<ratio; i++)
		{
			randInt = random.nextInt(cities.length-2)+1;
			
			tmpCity = intermediatePopulation[placeInPopulation][randInt];
			
			intermediatePopulation[placeInPopulation][randInt] = intermediatePopulation[placeInPopulation][randInt+1];
			
			intermediatePopulation[placeInPopulation][randInt+1] = tmpCity;
		}
	}
	
	/*
	 * This method picks a member from the current generation to be a parent of a member in the next generation..
	 */
	private static int rouletteWheel()
	{
		double totalFitness = 0.0;
		int returnInt = -1;
		for(int i = 0; i<population.length; i++)
		{
			totalFitness += routeFitness[i];
		}
		
        int randint = random.nextInt(1000001);
        double randDouble = randint / 1000000.0;
        double pointer = totalFitness * randDouble;
        double accumulatingFitness = 0.0;
	
        for(int i = 0; i<population.length; i++)
        {
        	accumulatingFitness += routeFitness[i];
        	
        	if(pointer < accumulatingFitness)
        	{
        		returnInt = i;
        		i = population.length;
        	}
        }
        
        return returnInt;
        
	}
	
	/*
	 * This method prints out the evaluated population showing details such as:
	 * -The routes in the population
	 * -The total distance associated with each of the routes
	 * -The fitness associated with each of the routes
	 * 
	 * And also highlights which member of the population has the best fitness.
	 */
	private static void printEvaluatedPopulation()
	{
		System.out.println("--------Generation "+generation+"--------");
	
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			if(i == bestRoute)
			{
				System.out.print("Best so far: ");
			}else
			{
				System.out.print("             ");
			}
			
			for(int j = 0; j<cities.length+1; j++)
			{
				if(j==0)
				{
					System.out.print(population[i][j]);
				}else
				{
					System.out.print("-->" + population[i][j]);
				}
			}
			if(routeDistance[i]>999)
			{
				System.out.println("         Distance: "+ routeDistance[i] +"        Fitness: "+routeFitness[i]);
			}else
			{
				System.out.println("         Distance: "+ routeDistance[i] +"         Fitness: "+routeFitness[i]);
			}
			
		}
		System.out.println();
	}
	
	
	public static void main(String [] args) throws IOException
	{
		
		createMatrices();
		
		population = new String [MAX_POPULATION][cities.length+1];
		intermediatePopulation = new String [MAX_POPULATION][cities.length+1];
		//printDistanceMatrices();
		
		while(generation <= MAX_GENERATIONS)
		{
			if(generation == 1)
			{
				initialisePopulation();
				evaluatePopulation();
				printEvaluatedPopulation();
			}else
			{
				generateNewGeneration();
				evaluatePopulation();
				printEvaluatedPopulation();
			}
			
			generation++;
		}
		
	}
	
	
	
	/**
	 * Methods below this point are for test purposes.
	 */
	
	//for checking the contents of the distance and cities arrays.
	private static void printDistanceMatrices()
	{
		for(int i = 0; i<cities.length;i++)
		{
			System.out.print(cities[i]+"\t");
		}
		System.out.println();
		for(int i = 0; i<distance.length; i++)
		{
			for(int j = 0; j<distance.length; j++)
			{
				System.out.print(distance[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	//for checking the contents of the population array.
	private static void printPopulation(boolean distances)
	{
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			System.out.print(population[i][0]);
			for(int j = 0; j<cities.length; j++)
			{
				if(distances == true) //distances = true (include distances between cities in route)
				{
					System.out.print("--(" + calculateDistance(population[i][j+1], intermediatePopulation[i][j]) + ")->" + population[i][j+1]);
				}else
				{
					System.out.print("-->" + population[i][j+1]);
				}
			}
			System.out.println();
		}
	}
	
	//for checking the contents of the intermediatePopulation array.
	private static void printIntermediatePopulation(boolean distances)
	{
		
		for(int i = 0; i<MAX_POPULATION; i++)
		{
			System.out.print(intermediatePopulation[i][0]);
			for(int j = 0; j<cities.length; j++)
			{
				if(distances == true) //distances = true (include distances between cities in route)
				{
					System.out.print("--(" + calculateDistance(intermediatePopulation[i][j+1], intermediatePopulation[i][j]) + ")->" + intermediatePopulation[i][j+1]);
				}else
				{
					System.out.print("-->" + intermediatePopulation[i][j+1]);
				}
			}
			System.out.println();
		}
	}
}
