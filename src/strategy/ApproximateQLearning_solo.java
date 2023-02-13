package strategy;

import java.util.ArrayList;

import java.util.Random;


import agent.Snake;
import item.Item;
import model.SnakeGame;

import utils.AgentAction;
import utils.ItemType;


public class ApproximateQLearning_solo extends Strategy{

	
	

    public ApproximateQLearning_solo(int nbActions, double epsilon, double gamma, double alpha) {	
        super(nbActions, epsilon, gamma, alpha);

  
    }
    
	
    
	@Override
	public synchronized AgentAction chooseAction(int idxSnake, SnakeGame snakeGame) {

		return AgentAction.MOVE_DOWN;

    }
	
	

	@Override
	public synchronized void update(int idx, SnakeGame state, AgentAction action, SnakeGame nextState, int reward, boolean isFinalState) {
		

	}
	
	

	
	

}
