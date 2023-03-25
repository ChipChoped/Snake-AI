package strategy;

import java.util.ArrayList;

import java.util.Random;


import agent.Snake;
import model.SnakeGame;

import utils.AgentAction;
import utils.Position;

import static java.lang.Math.abs;


public class ApproximateQLearning_solo extends Strategy{
	private final int d = 3;
	private final double[] w;
	private final Random rand = new Random();

	private double[] current_f;


    public ApproximateQLearning_solo(int nbActions, double epsilon, double gamma, double alpha) {
        super(nbActions, epsilon, gamma, alpha);

		this.w = new double[d+1];
		this.w[0] = rand.nextGaussian();
		this.w[1] = rand.nextGaussian();
		this.w[2] = rand.nextGaussian();
		this.w[3] = rand.nextGaussian();
    }


	private int manhattanDistance(Position snakeHead, Position entity, Position map, boolean withWalls, int x, int y) {
		int i = snakeHead.getX() + x - entity.getX();
		int j = snakeHead.getY() + y - entity.getY();

		if (!withWalls) {
			if (snakeHead.getX() + x == map.getX())
				return abs(- entity.getX()) + abs(j);
			else if (snakeHead.getX() + x == -1)
				return abs(map.getX() - 1 - entity.getX()) + abs(j);
			else if (snakeHead.getY() == map.getY())
				return abs(i) + abs(- entity.getY());
			else if (snakeHead.getY() == -1)
				return abs(i) + abs(map.getY() - 1 - entity.getY());
		}

		return abs(i) + abs(j);
	}


	private boolean isBodyObstacle(Position snake, Position apple, ArrayList<Position> body) {
		boolean isObstacle = false;

		for (int i = 4; i < body.size() - 1; i++) {
			isObstacle = isObstacle ||
					(snake.getX() < body.get(i).getX() && body.get(i).getX() < apple.getX() && snake.getX() == body.get(i).getY()) ||
					(snake.getX() > body.get(i).getX() && body.get(i).getX() > apple.getX() && snake.getX() == body.get(i).getY()) ||
					(snake.getY() < body.get(i).getY() && body.get(i).getY() < apple.getY() && snake.getY() == body.get(i).getY()) ||
					(snake.getY() > body.get(i).getY() && body.get(i).getY() > apple.getY() && snake.getY() == body.get(i).getY());
		}

		return isObstacle;
	}


	public double[] extractFeatures(SnakeGame state, AgentAction action) {
		double[] f = new double[d+1];

		f[0] = 1;

		int x = 0;
		int y = 0;

		switch (action) {
			case MOVE_UP:
				y = -1;
				break;
			case MOVE_DOWN:
				y = 1;
				break;
			case MOVE_LEFT:
				x = -1;
				break;
			case MOVE_RIGHT:
				x = 1;
				break;
		}

		Snake snake = state.getSnakes().get(0);

		Position snakeHeadPosition = new Position(snake.getX(), snake.getY());
		Position applePosition = new Position(state.getItems().get(0).getX(), state.getItems().get(0).getY());
		Position mapSize = new Position(state.getSizeX(), state.getSizeY());
		boolean withWalls = state.getWalls()[0][0];

		f[1] = manhattanDistance(snakeHeadPosition, applePosition, mapSize, withWalls, x, y);

		int nearBodyParts = 0;

		for (int i = 4; i < snake.getPositions().size() - 1; i++) {
			if (snake.getX() + x == snake.getPositions().get(i+1).getX()
						&& (snake.getY() + y + 1 == snake.getPositions().get(i+1).getY()
						|| snake.getY() + y - 1 == snake.getPositions().get(i+1).getY())
					|| snake.getY() + y == snake.getPositions().get(i+1).getY()
						&& (snake.getX() + x + 1 == snake.getPositions().get(i+1).getX()
						|| snake.getX() + x - 1 == snake.getPositions().get(i+1).getX()))
				nearBodyParts++;
		}

		f[2] = nearBodyParts;

		int nearWalls = 0;

		if (state.getWalls()[0][0]) {
			if (snake.getX() + x == 1 || snake.getX() + x == state.getSizeX() - 2)
				nearWalls++;

			if (snake.getY() + y == 1 || snake.getY() + y == state.getSizeY() - 2)
				nearWalls++;
		}

		f[3] = nearWalls;

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
		double[][] features = new double[4][d+1];
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
		double[][] features = new double[4][d+1];
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
