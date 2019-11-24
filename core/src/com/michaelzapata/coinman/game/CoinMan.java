package com.michaelzapata.coinman.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

import javax.xml.soap.Text;

public class CoinMan extends ApplicationAdapter {

	/** Textures are used TO ADD any sort of VISUAL imgage/asset - MADE WITH COLORS OR IMAGES
	 *  SpriteBatch - PUT SOMETHING ON THE SCREEN
	 *
	 *  Rectangle -> Object that will contain an image. -> X-axisPosition, Y-axis Position, width, height
	 *  Intersector.overlaps(obj, obj)//Checks if two objects, eg: Rectangle, overlap
	 */

	int gameState = 0;//1: alive, 0: Waiting to Start, anything else: Game over
	int lifes = 1;

	SpriteBatch batch;//PUT SOMETHING ON THE SCREEN
	Texture background;// Hold image of the 'background'
    Texture[] man;//Hold an Array of images
	Texture manDizzy;//Hold ONE image of a dizzy man
	Rectangle manRectangle; //Will hold the man Texture/Image to identify collisions
    //ShapeRenderer manRectangleShapeRenderer;//Create a border on the rectangle

	//Display text on Screen:
	BitmapFont font;

	//JAVA random object
	Random random = new Random();

	//Initial man state-> frame-1.png
	int manStateFrameImage = 0;
	//variable to slow Down the man state change
	int slowDown = 0;

	//define how QUICKLY man will fall -> GRAVITY
	float gravity = 0.4f;
	//define the fallVelocity at which the man will fall;
	float fallVelocity = 0;
	//Represents where the man is going to be in the SCREEN - Y axis
	float manYPosition;

	//Velocity at which bombs/coins move from RIGHT to left
	int xAxisVelocityMovement = 10;

	//COINS-----------------------------------------------------------
	int coinsPicked = 0;
	ArrayList<Integer> coinXPositions = new ArrayList<Integer>();
	ArrayList<Integer> coinYPositions = new ArrayList<Integer>();
	ArrayList<Rectangle> coinRectangles = new ArrayList<Rectangle>();
	Texture coin;
	int coinCount = 0;
	int coinFrecuency = 50;//we'll use it to make a coin every 100 iterations

	private void makeCoin(){
		float height = random.nextFloat() * Gdx.graphics.getHeight();//MAX: Gdx.graphics.getHeight(), which is Y-axis (TOP) ||| MIN: 0, which is Y-axis (BOTTOM)

		// Y position -> add the height positions to coinYPositions array
		coinYPositions.add((int) height);

		// X position-> will be the same every single time, OFF screen.
		// Gdx.graphics.getWidth() -> RIGHT-MOST because REMEMBER that the bottomLeftCorner point will be the (0,0) position of the IMAGE man
		// Therefore, if image is at Gdx.graphics.getWidth() POSITION in X axis, then it will be OFF SCREEN
		coinXPositions.add(Gdx.graphics.getWidth());

	}

	private void removeCoin(int index){
		coinRectangles.remove(index);
		coinYPositions.remove(index);
		coinXPositions.remove(index);
	}

	//BOMBS-----------------------------------------------------------
	int bombsPicked = 0;
	ArrayList<Integer> bombXPositions = new ArrayList<Integer>();
	ArrayList<Integer> bombYPositions = new ArrayList<Integer>();
	ArrayList<Rectangle> bombRectangles = new ArrayList<Rectangle>();
	Texture bomb;
	int bombCount = 0;
	int bombFrecueny = 1000;//we'll use it to make a bomb every 1000 iterations

	private void makeBomb(){
		float height = random.nextFloat() * Gdx.graphics.getHeight();//MAX: Gdx.graphics.getHeight(), which is Y-axis (TOP) ||| MIN: 0, which is Y-axis (BOTTOM)

		// Y position -> add the height positions to bombYPositions array
		bombYPositions.add((int) height);

		// X position-> will be the same every single time, OFF screen.
		// Gdx.graphics.getWidth() -> RIGHT-MOST because REMEMBER that the bottomLeftCorner point will be the (0,0) position of the IMAGE man
		// Therefore, if image is at Gdx.graphics.getWidth() POSITION in X axis, then it will be OFF SCREEN
		bombXPositions.add(Gdx.graphics.getWidth());

	}

	private void removeBomb(int index){
		bombRectangles.remove(index);
		bombYPositions.remove(index);
		bombXPositions.remove(index);
	}

	//CALLED ONCE, THE FIRST TIME YOU OPEN THE GAME.
	@Override
	public void create () {
	    //init the SpriteBatch
		batch = new SpriteBatch();
		//init the image background
		background = new Texture("bg.png");
		// init the array and its contents
        man = new Texture[4];
        man[0] = new Texture("frame-1.png");
        man[1] = new Texture("frame-2.png");
        man[2] = new Texture("frame-3.png");
        man[3] = new Texture("frame-4.png");

        //init dizzy picture
		manDizzy = new Texture("dizzy-1.png");

        //Create a border on the rectangle
		//manRectangleShapeRenderer = new ShapeRenderer();

        //init initial manYPosition
		manYPosition = Gdx.graphics.getHeight() / 2;//middle

		//init coin image
		coin = new Texture("coin.png");

		//init bomb image
		bomb = new Texture("bomb.png");

		//Make text on screen
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(5);//sets font SIZE
	}

	//CALLED, OVER AND OVER AND OVER, TILL YOU FINISH YOUR GAME - LOOP
    //OJO ---> The ORDER in which you draw, determines the z-index(who goes on top of who)
	@Override
	public void render () {
//		Gdx.gl.glClearColor(1, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//---------------------------------------------------------------------------------------------------------
		//1) we need to begin the SpriteBatch
		batch.begin();

		//put image background, startingPosition X, startingPosition Y, Image width, Image height,
		//Gdx.graphics.getHeight() -> get the height of the SCREEN
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//---------------------------------------------------------------------------------------------------------
		//put the man in the screen in the middle of the screen:
		// middleWidth: totalWidth/2, middleHeight: totalHeight/2
		//BUT you need to consider than an image starts at the bottomLeftCorner, so you need to substract an OFFSET;
		int manOffsetMiddlePoint = man[manStateFrameImage].getWidth()/2;
		int fixedManXPosition = Gdx.graphics.getWidth()/2  - manOffsetMiddlePoint;

		if(gameState == 2){
			batch.draw(manDizzy, fixedManXPosition, manYPosition);
		} else {
			batch.draw(man[manStateFrameImage], fixedManXPosition, manYPosition);
			// YOU don't need to set the width or height of THIS IMage:  Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			// we'll USE THE DEFAULT one
			//Put a Rectangle around it
			manRectangle = new Rectangle(fixedManXPosition,manYPosition,man[manStateFrameImage].getWidth(),man[manStateFrameImage].getHeight());
		}

//---------------------------------------------------------------------------------------------------------

		if(gameState == 1){
			//GAME IS ALIVE
			gameAlive();
		} else if(gameState == 0){
			//GAME IS WAITING TO START
			//If user touches the screen start game
			if(Gdx.input.justTouched()){
				gameState = 1;
			}
		} else {
			//GAME OVER
			//Reset game, Go back to Starting position:
			if(Gdx.input.justTouched()){
				resetGame();
			}

		}

		//---------------------------------------------------------------------------------------------------------
		//FINAL) we need to end the SpriteBatch
		batch.end();

		//Add a border to the manRectangle----------------------------------------
		//IT HAS TO BE AFTER: batch.end();
//		manRectangleShapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
//		manRectangleShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//		manRectangleShapeRenderer.setColor(new Color(0,0,1,0));
//		manRectangleShapeRenderer.rect(manRectangle.getX(),manRectangle.getY(),manRectangle.getWidth(),manRectangle.getHeight());
//		manRectangleShapeRenderer.end();
		//---------------------------------------------------------------------
		//Add a border to the coinRectangles----------------------------------------
		//IT HAS TO BE AFTER: batch.end();
		//CHECK COLLISION COIN
//		for (int i = 0; i < coinRectangles.size(); i++) {
//			ShapeRenderer tempSp = new ShapeRenderer();
//			tempSp.setProjectionMatrix(batch.getProjectionMatrix());
//			tempSp.begin(ShapeRenderer.ShapeType.Line);
//			tempSp.setColor(new Color(0,0,1,0));
//			tempSp.rect(coinRectangles.get(i).getX(),coinRectangles.get(i).getY(),coinRectangles.get(i).getWidth(),coinRectangles.get(i).getHeight());
//			tempSp.end();
//		}

		//---------------------------------------------------------------------

	}

	private void gameAlive(){
//RECOGNIZE, in the loop, at which moment USER touched the screen.
		// Gdx.input.justTouched() -> will be true, in this iteration, if user touched the screen,
		// AND it will be false, in the next iteration, if user doesn't touch the screen.(true/false) state is managed by Gdx.
		if(Gdx.input.justTouched()){
			fallVelocity = -20;// since -10-> man will jump;EVERY TIME you touch the screen.
		}
//---------------------------------------------------------------------------
		// initial fallVelocity will be 0;
		//fallVelocity = current fallVelocity + gravity;
		// The bigger the gravity, the faster it will fall
		fallVelocity = fallVelocity + gravity;

		//Position of man in the Y axis = current Y position - fallVelocity
		//The bigger the fall velocity -> the lower in the Y axis the man will be
		manYPosition = manYPosition - fallVelocity;

		//if man is at the bottom of the SCREEN (0) or Below
		// then -> keep him at position 0 (bottom of the screen)
		// REMEMBER. the bottomLeftCorner point will be the (0,0) position of the IMAGE man(not screen)
		// Therefore, this will keep the man running at the bottom of the screen.(THE whole body will be visible).
		//if man is at the bottom of the SCREEN (0) or Below
		if(manYPosition <= 0){
			manYPosition = 0; //keep him there.
		}

		//if man is at the top of the SCREEN (POSITION: Gdx.graphics.getHeight()) or HIGHER
		// then -> keep him at position Gdx.graphics.getHeight() (TOP of the screen)
		// REMEMBER. the bottomLeftCorner point will be the (0,0) position of the IMAGE man(not screen)

		// Therefore, we need to calculate the position before it goes over the ROOF (Gdx.graphics.getHeight()).
		int manYPosAtTOP = Gdx.graphics.getHeight() - man[manStateFrameImage].getHeight();
		//if we got to the Screen or above
		if(manYPosition >= manYPosAtTOP){
			manYPosition = manYPosAtTOP;//keep him there
		}

//---------------------------------------------------------------------------------------------------------

		//UPDATE THE man frame, EVERY 8 iterations, otherwise, too fast
		if(slowDown < 8) {
			slowDown++;
		} else {
			//RESET THE slowDown variable., so we only execute the code below, every 8 iterations
			slowDown = 0;
			//if we are using frame-1, frame-2 or frame-3 -> increase
			if(manStateFrameImage < 3){
				manStateFrameImage++;
			} else { // ONCE, manStateFrameImage == 3, set it back to 0
				manStateFrameImage = 0;
			}
		}

		//SHOW COINS---------------------------------------------------------------------------------------------------------
		//every 100 iteration, makeCoin in Screen.
		if(coinCount < coinFrecuency){
			coinCount++;
		} else{
			coinCount = 0;
			//This function will add RANDOM positions to the coinYPositions array, and the same position to the coinXPositions
			makeCoin();
		}

		//CLEAR THE coinRectangles array with every iteration, so we always keep one state of the coins' images
		coinRectangles.clear();
		// Iterate through all COINS present in screen, which are the ones that we have added in the makeCoin() method.
		for (int i = 0; i < coinXPositions.size(); i++) {
			//X-axis Position of the coin
			int currentCoinXPosition = coinXPositions.get(i);
			//MAKE THE COINS BE PRESENT, even though, WE SET X-position to be OFF SCREEN above.
			batch.draw(coin, currentCoinXPosition, coinYPositions.get(i));
			//THERFORE, we need to make sure we move the coins;
			//UPDATE, the current coin's position to move by an offset/separation of 4;
			int newCoinXPosition = currentCoinXPosition - xAxisVelocityMovement;
			coinXPositions.set(i, newCoinXPosition);

			//ADD, the Rectangle surrounding the image COIN
			coinRectangles.add(new Rectangle(currentCoinXPosition,coinYPositions.get(i),coin.getWidth(), coin.getHeight()));
		}

		//SHOW BOMBS-----------------------------------------------------------

		if(bombCount < bombFrecueny){
			bombCount++;
		} else {
			bombCount=0;
			makeBomb();
		}

		//CLEAR THE bombRectangles array with every iteration, so we always keep one state of the bombs' images
		bombRectangles.clear();
		// Iterate through all BOMBS present in screen, which are the ones that we have added in the makeBomb() method.
		for (int i = 0; i < bombXPositions.size(); i++) {
			//X-axis Position of the coin
			int currentBombXPosition = bombXPositions.get(i);
			//MAKE THE BOMBS BE PRESENT, even though, WE SET X-position to be OFF SCREEN above.
			batch.draw(bomb, currentBombXPosition, bombYPositions.get(i));
			//THERFORE, we need to make sure we move the BOMBS;
			//UPDATE, the current bombs' position to move by an offset/separation of 4;
			int newBombXPosition = currentBombXPosition - xAxisVelocityMovement;
			bombXPositions.set(i, newBombXPosition);

			//ADD, the Rectangle surrounding the image BOMB
			bombRectangles.add(new Rectangle(currentBombXPosition,bombYPositions.get(i),bomb.getWidth(), bomb.getHeight()));
		}

		//CHECK COLLISION COIN
		for (int i = 0; i < coinRectangles.size(); i++) {
			//if manRectangle collides with any Coin Rectangle
			if (Intersector.overlaps(manRectangle, coinRectangles.get(i))) {
				coinsPicked++;
				removeCoin(i);

				//break;//ONLY use if Coins will not be TOO close;
				//otherwise, you may not collect 2 coins if they are close
			}
		}

		//CHECK COLLISION BOMB
		for (int i = 0; i < bombRectangles.size(); i++) {
			//if manRectangle collides with any Coin Rectangle
			if (Intersector.overlaps(manRectangle, bombRectangles.get(i))) {
				bombsPicked++;
				removeBomb(i);
			}
		}


		//if dead: CHANGE gameState:
		if(lifes <= bombsPicked){
			gameState = 2;
		}

		//add text:
		font.draw(batch, "Coins collected: " + String.valueOf(coinsPicked), 50, Gdx.graphics.getHeight() - 50);
		font.draw(batch, "Lifes left: " + ( lifes - bombsPicked), 50, Gdx.graphics.getHeight() - 200);

	}

	private void coinsBombsAndMan(){

	}

	private void resetGame(){
		gameState = 1;
		manYPosition = Gdx.graphics.getHeight() / 2;
		coinsPicked = 0;
		bombsPicked = 0;
		fallVelocity = 0;
		coinXPositions.clear();
		coinYPositions.clear();
		coinRectangles.clear();
		coinCount = 0;
		bombXPositions.clear();
		bombYPositions.clear();
		bombRectangles.clear();
		bombCount = 0;
	}

	//END THE GAME AND START A NEW ONE
	@Override
	public void dispose () {
		batch.dispose();
		background.dispose();
	}
}
