package strategy;

import java.util.ArrayList;

import java.util.Random;


import agent.Snake;
import item.Item;
import model.SnakeGame;

import utils.AgentAction;
import utils.ItemType;
import utils.Position;

import static java.lang.Math.abs;


public class ApproximateQLearning_solo extends Strategy{
	private final int d = 1;
	private final double[] w;
	private final Random rand = new Random();

	private double[] current_f;


    public ApproximateQLearning_solo(int nbActions, double epsilon, double gamma, double alpha) {
        super(nbActions, epsilon, gamma, alpha);

		this.w = new double[d+1];
		this.w[0] = rand.nextGaussian();
		this.w[1] = rand.nextGaussian();
    }


	private int manhattanDistance(SnakeGame state, int x, int y) {
		Snake snake = state.getSnakes().get(0);
		Item apple = state.getItems().get(0);

		int width = state.getSizeX();
		int height = state.getSizeY();

		int i = snake.getX() + x - apple.getX();
		int j = snake.getY() + y - apple.getY();

		if (!state.getWalls()[0][0]) {
			if (snake.getX() + x == width)
				return abs(- apple.getX()) + abs(j);
			else if (snake.getX() + x == -1)
				return abs(width - 1 - apple.getX()) + abs(j);
			else if (snake.getY() == height)
				return abs(i) + abs(- apple.getY());
			else if (snake.getY() == -1)
				return abs(i) + abs(height - 1 - apple.getY());
		}

		return abs(i) + abs(j);
	}


	public double[] extractFeatures(SnakeGame state, AgentAction action) {
		double[] f = new double[d+1];

		f[0] = 1;

		switch (action) {
			case MOVE_UP:
				f[1] = manhattanDistance(state, 0, -1);
				break;
			case MOVE_DOWN:
				f[1] = manhattanDistance(state, 0, 1);
				break;
			case MOVE_LEFT:
				f[1] = manhattanDistance(state, -1, 0);
				break;
			case MOVE_RIGHT:
				f[1] = manhattanDistance(state, 1, 0);
				break;
		}

		return f;
	}


	public double scalarProduct(double[] w, double[] f) {
		double q = 0;

		for (int i = 0; i < w.length; i++)
			q += w[i] * f[i];

		return q;
	}


	public int argMax(double[] args)
	{
		if (args[0] == args[1] && args[1] == args[2] && args[2] == args[3])
			return rand.nextInt(4);
		else {
			int maxIndex = 0;
			double maxArg = args[0];

			for (int i = 1; i < args.length; i++) {
				if (args[i] > maxArg) {
					maxIndex = i;
					maxArg = args[i];
				}
			}

			return maxIndex;
		}
	}


	private AgentAction actionCases(SnakeGame snakeGame, int actionValue) {
		switch (actionValue) {
			case 0:
				return AgentAction.MOVE_UP;
			case 1:
				return AgentAction.MOVE_DOWN;
			case 2:
				return AgentAction.MOVE_LEFT;
			case 3:
				return AgentAction.MOVE_RIGHT;
			default:
				return snakeGame.getSnakes().get(0).getLastMove();
		}
	}


	@Override
	public synchronized AgentAction chooseAction(int idxSnake, SnakeGame state) {
		double[][] features = new double[4][1];
		double[] QStates = new double[4];

		features[0] = extractFeatures(state, AgentAction.MOVE_UP);
		QStates[0] = scalarProduct(w, features[0]);

		features[1] = extractFeatures(state, AgentAction.MOVE_DOWN);
		QStates[1] = scalarProduct(w, features[1]);

		features[2] = extractFeatures(state, AgentAction.MOVE_LEFT);
		QStates[2] = scalarProduct(w, features[2]);

		features[3] = extractFeatures(state, AgentAction.MOVE_RIGHT);
		QStates[3] = scalarProduct(w, features[3]);

		if (rand.nextDouble() < this.epsilon) {
			int action = rand.nextInt(4);
			current_f = features[action];

			return actionCases(state, action);
		}
		else {
			int indexMax = argMax(QStates);
			current_f = features[indexMax];

			return actionCases(state, indexMax);
		}
    }


	@Override
	public synchronized void update(int idx, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {
		double[][] features = new double[4][1];
		double[] QNewStates = new double[4];

		features[0] = extractFeatures(state, AgentAction.MOVE_UP);
		QNewStates[0] = scalarProduct(w, features[0]);

		features[1] = extractFeatures(state, AgentAction.MOVE_DOWN);
		QNewStates[1] = scalarProduct(w, features[1]);

		features[2] = extractFeatures(state, AgentAction.MOVE_LEFT);
		QNewStates[2] = scalarProduct(w, features[2]);

		features[3] = extractFeatures(state, AgentAction.MOVE_RIGHT);
		QNewStates[3] = scalarProduct(w, features[3]);

		double target = reward + gamma * QNewStates[argMax(QNewStates)];
		double QState = scalarProduct(w, current_f);

		for (int i = 0; i < d + 1; i++)
			w[i] = w[i] - 2 * alpha * current_f[i] * (QState - target);
	}


}
