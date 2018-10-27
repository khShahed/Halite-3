// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.*;

public class MyBot {

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        game.ready("MyJavaBot");

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();
            List<Direction> directionOrder = Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.STILL);
            for (final Ship ship : me.ships.values()) {
                ArrayList<Position> positonOptions = ship.position.getSurroundingCardinals();
                positonOptions.add(ship.position);

                // This HashMap will contains direction and position
                // 'n': {19:38}
                HashMap<Direction, Position> positionDictionary = new HashMap<>();

                // This HashMap will contains Positions and Amount of halite in that position
                // {0,1}: 500
                HashMap<Position, Integer> haliteDictionary = new HashMap<>();

                // Populate positionDictionary
                for(Direction direction: directionOrder) {
                    positionDictionary.put(direction, positonOptions.get(directionOrder.indexOf(direction)));
                }

                // Populate haliteDictionary with halite amount and position
                for (Direction direction : positionDictionary.keySet()) {
                    Position position = positionDictionary.get(direction);
                    int halite = gameMap.at(position).halite;
                    haliteDictionary.put(position, halite);
                }

                Integer maxHalite =
                        haliteDictionary.values()
                            .stream()
                            .max((entry1, entry2) -> entry1 > entry2 ? 1 : -1)
                            .get();
                Position maxHalitePositon = getKeyByValue(haliteDictionary, maxHalite);
                Direction maxHaliteDirection = getKeyByValue(positionDictionary, maxHalitePositon);


                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    commandQueue.add(ship.move(maxHaliteDirection));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            }

            if (
                game.turnNumber <= 200 &&
                me.halite >= Constants.SHIP_COST &&
                !gameMap.at(me.shipyard).isOccupied())
            {
                commandQueue.add(me.shipyard.spawn());
            }

            game.endTurn(commandQueue);
        }
    }
}
