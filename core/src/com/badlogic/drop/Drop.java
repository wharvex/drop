package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

public class Drop extends ApplicationAdapter {
  Texture img;
  private Texture bucketImage;
  private Sound dropSound;
  private Music rainMusic;
  private Texture dropImage;
  private OrthographicCamera camera;
  private SpriteBatch batch;
  private Rectangle bucket;
  private long lastDropTime;

  // The LibGDX Array minimizes garbage as much as possible, as opposed to the Java ArrayList.
  private Array<Rectangle> raindrops;

  @Override
  public void create() {
    // Load the images for the droplet and the bucket, 64x64 pixels each.
    dropImage = new Texture(Gdx.files.internal("drop.png"));
    bucketImage = new Texture(Gdx.files.internal("bucket.png"));

    // Load the drop sound effect and the rain background music.
    dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
    rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

    // Start the playback of the background music immediately.
    rainMusic.setLooping(true);
    rainMusic.play();

    // Create the camera.
    camera = new OrthographicCamera();
    camera.setToOrtho(false, 800, 480);

    // Create the sprite batch.
    batch = new SpriteBatch();

    // Instantiate the Rectangle that represents our bucket, and specify its initial values.
    bucket = new Rectangle();
    bucket.x = (float) 800 / 2 - (float) 64 / 2;
    bucket.y = 20;
    bucket.width = 64;
    bucket.height = 64;

    raindrops = new Array<>();
    spawnRaindrop();
  }

  @Override
  public void render() {
    ScreenUtils.clear(0, 0, 0.2f, 1);
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    batch.draw(bucketImage, bucket.x, bucket.y);
    for (Rectangle raindrop : raindrops) {
      batch.draw(dropImage, raindrop.x, raindrop.y);
    }
    batch.end();

    // If the user touches the screen (or presses a mouse button), we want the bucket to center
    // around that position horizontally.
    if (Gdx.input.isTouched()) {
      // Create a 3-dimensional vector representing the touch position.
      Vector3 touchPos = new Vector3();

      // Set the touch position coordinates.
      touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

      // Transform the touch position coordinates to our camera's coordinate system.
      camera.unproject(touchPos);

      // Change the position of the bucket to be centered around the touch/mouse coordinates.
      bucket.x = touchPos.x - (float) 64 / 2;
    }

    // We want the bucket to move without acceleration, at two hundred pixels/units per second,
    // either to the left or the right, when the left or right arrow keys are pressed.
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      bucket.x -= 200 * Gdx.graphics.getDeltaTime();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      bucket.x += 200 * Gdx.graphics.getDeltaTime();
    }

    // We also need to make sure our bucket stays within the screen limits.
    if (bucket.x < 0) {
      bucket.x = 0;
    }
    if (bucket.x > 800 - 64) {
      bucket.x = 800 - 64;
    }

    // Check how much time has passed since we spawned a new raindrop, and create a new one if
    // necessary.
    if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
      spawnRaindrop();
    }

    // The raindrop update loop.
    // Make our raindrops move at a constant speed of 200 pixels/units per second.
    // If the raindrop is beneath the bottom edge of the screen, we remove it from the array.
    for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
      Rectangle raindrop = iter.next();
      raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
      if (raindrop.y + 64 < 0) {
        iter.remove();
      }
      if (raindrop.overlaps(bucket)) {
        dropSound.play();
        iter.remove();
      }
    }
  }

  /**
   * Instantiates a new Rectangle, sets it to a random position at the top edge of the screen and
   * adds it to the raindrops array.
   */
  private void spawnRaindrop() {
    Rectangle raindrop = new Rectangle();
    raindrop.x = MathUtils.random(0, 800 - 64);
    raindrop.y = 480;
    raindrop.width = 64;
    raindrop.height = 64;
    raindrops.add(raindrop);
    lastDropTime = TimeUtils.nanoTime();
  }

  @Override
  public void dispose() {
    dropImage.dispose();
    bucketImage.dispose();
    dropSound.dispose();
    rainMusic.dispose();
    batch.dispose();
  }
}
