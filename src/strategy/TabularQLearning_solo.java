package strategy;


import java.util.HashMap;

import java.util.Random;

import agent.Snake;
import item.Item;
import model.SnakeGame;
import utils.AgentAction;
import utils.Position;


public class TabularQLearning_solo extends Strategy {
	HashMap<String, double[]> QMap;

    public TabularQLearning_solo(int nbActions, double epsilon, double gamma, double alpha) {	
        super(nbActions, epsilon, gamma, alpha);
		QMap = new HashMap<>();
    }

	@Override
	public synchronized AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {
		Random rand = new Random();

		if (rand.nextDouble() < epsilon)
			return actionCases(snakeGame, rand.nextInt(4));
		else {
			String state = encodeState(idxSnake, snakeGame);

			if (QMap.get(state) != null) {
				double max = QMap.get(state)[0];
				int QMax = 0;
				int equal = 1;

				for (int i = 1; i < QMap.get(state).length; i++) {
					if (QMap.get(state)[i] > max) {
						max = QMap.get(state)[i];
						QMax = i;
						equal++;
					}
				}

				if (equal == QMap.get(state).length)
					QMax = rand.nextInt(4);

				return actionCases(snakeGame, QMax);
			}
			else {
				QMap.put(state, new double[]{0.0, 0.0, 0.0, 0.0});
				return actionCases(snakeGame, rand.nextInt(4));
			}
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
	public synchronized void update(int idxSnake, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {
		String encodedState = encodeState(idxSnake, state);
		String encodedNextState = encodeState(idxSnake, nextState);;
		double maxNextState = 0.0;
		int actionNumber = -1;

		QMap.computeIfAbsent(encodedState, k -> new double[]{0.0, 0.0, 0.0, 0.0});

		if (QMap.get(encodedNextState) != null) {
			maxNextState = QMap.get(encodedNextState)[0];

			for (int i = 1; i < QMap.get(encodedNextState).length; i++)
				if (QMap.get(encodedNextState)[i] > maxNextState)
					maxNextState = QMap.get(encodedNextState)[i];
		} else QMap.put(encodedNextState, new double[]{0.0, 0.0, 0.0, 0.0});

		switch (action) {
			case MOVE_UP:
				actionNumber = 0;
				break;
			case MOVE_DOWN:
				actionNumber = 1;
				break;
			case MOVE_LEFT:
				actionNumber = 2;
				break;
			case MOVE_RIGHT:
				actionNumber = 3;
				break;
		}

		QMap.get(encodedState)[actionNumber] = (1 - alpha) * QMap.get(encodedState)[actionNumber] +
				alpha * (reward + gamma * maxNextState);
	}

	public String encodeState( int idxSnake, SnakeGame snakeGame) {
		StringBuilder state = new StringBuilder();
		Snake snake = snakeGame.getSnakes().get(idxSnake);
		Position head = snake.getPositions().get(0);
		Item apple = snakeGame.getItems().get(0);

		state.append("V".repeat(Math.max(0, snakeGame.getSizeX() * snakeGame.getSizeY())));
		state.setCharAt(snakeGame.getSizeX() * head.getY() + head.getX(), 'H');
		state.setCharAt(snakeGame.getSizeX() * apple.getY() + apple.getX(), 'A');

		for (int i = 1; i < snake.getPositions().size(); i++)
			state.setCharAt(snakeGame.getSizeX() * snake.getPositions().get(i).getY() + snake.getPositions().get(i).getX(), 'B');

		return state.toString();
	}
}
