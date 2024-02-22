/** Deadwood Studios: 
 *  Jacob McCabe, Sai Veeravelli.
 *  CSCI 345
 */
import java.util.*;
import java.util.function.ToLongBiFunction;
import java.lang.Integer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


public class Deadwood {

	public static void main(String[] args) throws Exception{
		Manager manager = new Manager();
	}
}

class GLOBAL {
	public static int activeSets;
}

// Controller
// The Manager class is responsible for conducting the flow of the game.
// It sets up and starts the game, tells each player when it is their turn,
// determines when a day ends, and will end the game.
class Manager {
	private static int MAXDAYS = 4;
	private static Player[] players;
	private static int days = 0;
	private static Board board;
	private static Deck deck;
	private static UserInterface view = new UserInterface();
	private static ParseXML parser;

	//constructor
	public Manager() throws Exception {
		this.parser = parser.getInstance();
		startUp();
	}

	//calles parser and sets up the board and calls methoid to make player
	private void startUp() throws Exception{
		if(days!=0) 
			return;
		Board bd = parser.parseBoard();
		setBoard(bd);
		deck = parser.parseCards();

		GLOBAL.activeSets = 10;

		Scanner scan = new Scanner(System.in);
		int numPlayers = 0;
		while(numPlayers<2 || numPlayers>8){
			view.printGetNumPlayers();
			numPlayers = scan.nextInt();
		}
		setPlayers(makePlayers(numPlayers, scan));
		if(numPlayers<=3)
			MAXDAYS = 3;

		startDay();
	}

	//makes askes users for name of player and makes player
	private Player[] makePlayers(int numPlayers, Scanner scan) throws Exception {
		Player[] temp = new Player[numPlayers];
		for(int i=0; i < numPlayers; i++){
			view.printGetPlayer(i+1);
			String name = scan.next();
			temp[i] = makePlayer(numPlayers, name);
		}
		return temp;
	}

	//Makes the players according to the special rules (dependent on
	//the number of people playing)
	private Player makePlayer(int numPlayers, String name) {
		Player player = null;
		switch(numPlayers){
			case 2: case 3: case 4:
			       	player = new Player(name, 0, 1);
				break;
			case 5: 
				player = new Player(name, 2, 1); 
				break;
			case 6: 
				player = new Player(name, 4, 1);
				break;
			case 7: case 8: 
				player = new Player(name,0, 2);
				break;
		}
		return player;
	}

	//runs the setups for the day
	private void startDay() {
		GLOBAL.activeSets = 10;
		days++;
		getBoard().setCards(deck);
		movePlayersToStart();
		startPlay();
	}

	//Runs the workings of each day, determing who's
	//turn is next and checking whether it is the end of a day.
	private void startPlay() {
		Player[] people = getPlayers();
		while (GLOBAL.activeSets > 1) {
			for (Player pl : people) {
				OnTurn turn = new OnTurn(pl);
			}
		}
		endDay();
	}
	
	//Determines whether it was the last day of play and
	//will reset the board for the next day.
	private void endDay() {
		checkLastDay();
		resetBoard();
	}

	//Used to determine how many in-game 'days' have been
	//played.
	private void checkLastDay() {
		if (getDays() == MAXDAYS) {
			endGame();
		}
	}

	//Moves all players to the Trailer for the
	//start of a new day.
	private void movePlayersToStart() {
		for (Player i : getPlayers()) {
			i.updateLocation(board.getTrailer());
		}	
	}

	private void resetBoard() {
		ArrayList<Room> layout = board.getLayout();
		for (Room rm : layout) {
			if (isSet(rm)) {
				Set set = (Set) rm;
				set.resetSet();
			}
		}
	}

	//claculates the highest score and says who won
	private void endGame(){
		int highestScore = 0;
		String[] names = new String[players.length+1];

		for(Player i : getPlayers()){
			i.updateScore();
			if(i.getScore()>highestScore){
				names = null;
				names[1] = i.getName();
				highestScore = i.getScore();
			}else if(i.getScore() == highestScore){
				names[names.length] = i.getName();
			}
		}
		String output="";
		for(String e : names){
			output+=e+", ";
		}
		System.out.printf(output+"Won with %d!!!\n",highestScore);
	}

	private int getDays() {
		return days;
	}

	public static Player[] getPlayers() {
		return players;
	}

	private static void setPlayers(Player[] playersIn) {
		players = playersIn;
	}

	public int numberPlayers() {
		return players.length;
	}

	public static Board getBoard(){
		return board;
	}

	private void setBoard(Board board) {
		this.board = board;
	}

	private void setDeck(Deck deck) {
		this.deck = deck;
	}

	//returns true of false if the object given is a Set
	public static boolean isSet(Room loc){
		return (!loc.getName().equals("Office") && !loc.getName().equals("Trailer"));
	}

	//returns ture or false if the object given is a Office
	public static boolean isOffice(Room loc){
		return (loc.getName().equals("Office"));

	}

	//returns ture or false if the object given is a Trailer
	public static boolean isTrailer(Room loc){
		return (loc.getName().equals("Trailer"));
	}
}

/**
 * Room
 * 
 * implimented by
 * set
 * upgrade
 * trailer
 * 
 * ----
 * card tight relation to set
 */

//Interface for Room objects
interface Room {
	String name = "";
	String[] neighbors = {};
	int[] area = {};
	ArrayList<Player> playersHere = new ArrayList<Player>();

	String getName();
	String[] getNeighbors();
	ArrayList<Player> getPlayers();
	void setPlayers(ArrayList<Player> playersArriving);
	boolean isNeighbor(Room Location);
	void updatePlayers(Player player);
}

//Class the deals with upgrading the players level
class Upgrade implements Room {
	private String name;
	private String[] neighbors;
	private int[] area;
	private ArrayList<Player> playersHere = new ArrayList<Player>();
	private ArrayList<Level> levels;

	public Upgrade(String name, String[] neighbors, int[] area, ArrayList<Level> levels) {
		this.name = name;
		this.neighbors = neighbors;
		this.area = area;
		this.levels = levels;
	}

	public String getName() {
		return this.name;
	}

	public String[] getNeighbors() {
		return this.neighbors;
	}

	public ArrayList<Player> getPlayers() {
		return this.playersHere;
	}

	public void setPlayers(ArrayList<Player> playersArriving) {
		for (Player i : playersArriving) {
			this.playersHere.add(i);
		}
	}

	public int getLevelAmount(int lvl, String curr) {
		int amt = -1;

		for (Level i: this.levels) {
			if ((i.getLevel() == lvl) && (i.getCurrency().equals(curr))) 
				amt = i.getAmount();
		}
		return amt;
	}
	
	public boolean isNeighbor(Room Location){
		for(String i : neighbors){
			if(Location.getName().equals(i))
				return true;
		}
		return false;
	}
	
	public void updatePlayers(Player player) {
		this.playersHere.add(player);
	}
}

// The Starting locations for the players at the end of eatch day
class Trailer implements Room {
	private String name;
	private String[] neighbors;
	private int[] area;
	private ArrayList<Player> playersHere;

	public Trailer(String name, String[] neighbors, int[] area) {
		this.name = name;
		this.neighbors = neighbors;
		this.area = area;
		playersHere = new ArrayList<Player>();
	}

	public String[] getNeighbors() {
		return this.neighbors;
	}


	public ArrayList<Player> getPlayers() {
		return playersHere;
	}

	public void setPlayers(ArrayList<Player> playersArriving) {
		for (Player i : playersArriving) {
			this.playersHere.add(i);
		}
	}

	public String getName() {
		return this.name;
	}

	public boolean isNeighbor(Room Location){
		for(String i : neighbors){
			if(Location.getName().equals(i)) return true;
		}
		return false;
	}

	public void updatePlayers(Player player) {
		playersHere.add(player);
	}
}

// Class the makes all the rooms where you can work
class Set implements Room {

	private String setName;
	private String[] neighbors;
	private int[] area;
	private int numTakes;
	private int[][] takes;
	private ArrayList<Role> parts;
	private ArrayList<Player> playersHere;
	private Card card;
	private boolean active;

	public Set(String setName, String[] neighbors, int[] area, int[][] takes, ArrayList<Role> parts) {
		this.setName = setName;
		this.neighbors = neighbors;
		this.area = area;
		this.takes = takes;
		numTakes = takes.length;
		this.parts = parts;
		playersHere = new ArrayList<Player>();
		active = true;
	}

	public ArrayList<Role> getParts() {
		return this.parts;
	}

	public ArrayList<Player> getPlayers() {
		return this.playersHere;
	}

	public Card getCard(){
		return card;
	}

	public void setCard(Card card){
		this.card = card;
	}

	private void setName(String setName) {
		this.setName = setName;
	}
	
	public void setPlayers(ArrayList<Player> playersArriving) {
		for (Player i : playersArriving) {
			playersHere.add(i);
		}
	}
	
	public String[] getNeighbors() {
		return this.neighbors;
	}

	public int getNumTakes() {
		return numTakes;
	}

	private void setNumTakes(int numTakes) {
		this.numTakes = numTakes;
	}

	public void successfulActing() {
		setNumTakes(numTakes - 1);
	}
	
	public String getName() {
		return this.setName;
	}

	public boolean getActive() {
		return active;
	}
	private void setActive(boolean active) {
		this.active = active;
	}

	public boolean isNeighbor(Room Location){
		for(String i : neighbors){
			if(Location.getName().equals(i)) return true;
		}

		return false;
	}
	public void wrapScene() {
		setActive(false);
		getCard().finishedCard();
		setCard(null);
		GLOBAL.activeSets--;
	}

	public Role getRoleFromList(String name, ArrayList<Role> roles) {
		for (Role rl : roles) {
			if (rl.getPartName().toLowerCase().equals(name)) {
				return rl;
			}
		}
		return null;
	}

	public void resetSet() {
		setActive(true);
		setNumTakes(takes[0].length);
		getCard().finishedCard();
	}

	public ArrayList<Role> getOpenRoles(ArrayList<Role> possible, ArrayList<Player> players) {
		ArrayList<Role> output = new ArrayList<Role>();
		for (Role rl : possible) {
			boolean occupied = false;
			for (Player pl : players) {
				if(isPlayerOnRole(pl, rl)) {
					occupied = true;
				}
			}
			if (!occupied) {
				output.add(rl);
			}
		}
		return output;
	}

	private boolean isPlayerOnRole(Player pl, Role rl) {
		if (pl.getRole() == null)
			return false;
		else if (pl.getRole().getPartName().equals(rl.getPartName())) {
			return true;
		}
		else
			return false;
	}

	public void updatePlayers(Player player) {
		playersHere.add(player);
	}

}

class Level {

	private int level;
	private String currency;
	private int amt;
	private int[] area;

	public Level(int level, String currency, int amt, int[] area) {
		this.level = level;
		this.currency = currency;
		this.amt = amt;
		this.area = area;
	}

	public int getLevel() {
		return this.level;
	}

	public int getAmount() {
		return this.amt;
	}

	public String getCurrency() {
		return this.currency;
	}
}

//Class that is the scenes for a set object
class Card {

	private String name;
	private int budget;
	private int id;
	private String desc;
	private ArrayList<Role> parts;
	private boolean isActive;

	public Card(String name, int budget, int id, ArrayList<Role> parts) {
		this.name = name;
		this.budget = budget;
		this.id = id;
		this.parts = parts;
		isActive = false;
	}

	public String getName() {
		return name;
	}

	public int getBudget() {
		return budget;
	}

	public ArrayList<Role> getParts() {
		return this.parts;
	}

	public String getDesc() {
		return this.desc;
	}

	public int getID() {
		return this.id;
	}

	private void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void finishedCard() {
		setIsActive(false);
	}

	//determines if a specified role is a card role or an extra
	public boolean isOnCardRole(Role role) {
		boolean onCard = false;

		for (Role i : getParts()) {
			if (i.getPartName().equals(role.getPartName())) {
				onCard = true;
			}
		}
		return onCard;
	}

}

// Controller
//coloection of cards
class Deck {

	private ArrayList<Card> cards;

	public Deck(ArrayList<Card> cards) {
		this.cards = cards;
	}

	public void shuffle() {
		Collections.shuffle(cards);
	}

	public Card drawCard() {
		shuffle();
		int end = cards.size() - 1;
		return cards.remove(end);
	}
}

// Model
//Roles for the the jobs on card
class Role {

	private String partName;
	private int level;
	private int area[];
	private String line;
	private boolean occupied;

	public Role(String partName, int level, int[] area, String line) {
		this.partName = partName;
		this.level = level;
		this.area = area;
		this.line = line;
		occupied = false;
	}

	public String getPartName() {
		return partName;
	}

	public int getLevel() {
		return level;
	}

	public String getLine() {
		return line;
	}
	
	public boolean getOccupied() {
		return occupied;
	}

}

// Model
// holds the players sets and cards
class Board {

	private String boardName;
	private ArrayList<Room> layout;

	public Board(String boardName, ArrayList<Room> layout) {
		this.boardName = boardName;
		this.layout = layout;
	}
	public void setCards(Deck deck){
		for(Room i : layout){
			if(Manager.isSet(i)){
				Set e = (Set) i;
				e.setCard(deck.drawCard());
			}
		}
	}

	public ArrayList<Room> getLayout() {
		return layout;
	}

	public Trailer getTrailer(){
		for(Room i : layout){
			if(Manager.isTrailer(i)) {
				return (Trailer)i;
			}
		}

		return null;
	}

	public Upgrade getUpgrade(){
		for(Room i : layout){
			if(Manager.isOffice(i)) {
				return (Upgrade)i;
			}
		}

		return null;
	}

	public Room getRoomByName(String goTo) {
		for(Room i : layout){
			if(i.getName().toLowerCase().equals(goTo)){
				return i;
			}
		}
		return null;
	}
}

// Model
// Player implimentation what the user plays as
class Player {

	private String name;
	private int rank;
	private Room located;
	private int dollars;
	private int credits;
	private Role activeRole;
	private int practiceChip;
	private int score;

	public Player(String name, int credits, int rank) {
		this.name = name;
		this.credits = credits;
		this.rank = rank;
		this.dollars =0;
		this.activeRole = null;
		this.score = 0;
		this.practiceChip = 0;
		this.located = Manager.getBoard().getTrailer();
	}


	public Integer getScore() {
		return score;
	}

	public void updateScore() {
		score = dollars+credits+(5*rank);
	}

	public String getName() {
		return name;
	}

	public int getRank() {
		return rank;
	}

	private void setRank(int rank) {
		this.rank = rank;
	}

	public Room getLocation() {
		return located;
	}

	private void setLocation(Room located) {
		this.located = located;
	}

	public int getDollars() {
		return dollars;
	}

	private void setDollars(int dollars) {
		this.dollars = dollars;
	}

	public void earnedDollars(int amt) { 
		setDollars(getDollars() + amt);
	}

	public int getCredits() {
		return credits;
	}

	private void setCredits(int credits) {
		this.credits = credits;
	}
	public void earnedCredits(int amt) { 
		setCredits(getCredits() + amt);
	}

	public Role getRole() {
		return activeRole;
	}

	private void setRole(Role activeRole) {
		this.activeRole = activeRole;
	}

	public int getPracticeChip() {
		return practiceChip;
	}

	public void addPracticeChip() {
		setPracticeChip(getPracticeChip() + 1);
	}
	private void setPracticeChip(int practiceChip) {
		this.practiceChip = practiceChip;
	}

	private boolean withdraw(String currency, int amt) {
		boolean valid = false;
		int bank = 0;
		switch (currency.toLowerCase()) {
			case "dollar":
				bank = getDollars();
				if (amt <= bank) {
					setDollars(bank - amt);
					valid = true;
				}
				break;
			case "credit":
				bank = getCredits();
				if (amt <= bank) {
					setCredits(bank - amt);
					valid = true;
				}
				break;
		}
		return valid;
	}


	public boolean requestUpgrade(String currency, int level) {
		Board board = Manager.getBoard();
		Upgrade office = (Upgrade) board.getUpgrade();
		
		boolean success = false;
		int request = office.getLevelAmount(level, currency);
		if (request == -1) {
			return success;
		}
		
		if (withdraw(currency, level)) {
			setRank(level);
			success = true;
		}

		return success;

	}

	public void updateLocation(Room Location){
		this.setLocation(Location);
	}

	public void updateRole(Role role) {
		setRole(role);
	}

	public void endScene() {
		setPracticeChip(0);
		updateRole(null);
	}

}

// Controller
class OnTurn {

	private Player player;
	private UserInterface view;
	private boolean hasMoved;
	private boolean hasRole;
	private boolean hasWorked;
	private boolean hasUpgraded;

	public OnTurn(Player player) {
		this.player = player;
		this.view = new UserInterface();
		hasMoved = (player.getRole() == null) ? false : true;
		hasRole = (player.getRole() == null) ? false : true;
		hasWorked = (player.getRole() == null) ? true : false;
		hasUpgraded = player.getLocation().getName().equals("office") ? true : false;
		turn(player);
	}

	public boolean getHasMoved() {
		return this.hasMoved;
	}

	private void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
	
	public boolean getHasRole() {
		return this.hasRole;
	}

	private void setHasRole(boolean hasRole) {
		this.hasRole = hasRole;
	}

	public boolean getHasWorked() {
		return this.hasWorked;
	}

	private void setHasWorked(boolean hasWorked) {
		this.hasWorked = hasWorked;
	}

	public boolean getHasUpgraded() {
		return this.hasUpgraded;
	}

	private void setHasUpgraded(boolean hasUpgraded) {
		this.hasUpgraded = hasUpgraded;
	}

	private void setPlayer(Player player) {
		this.player = player;
	}

	// Runs each player's turn
	// Each turn runs until 'end turn' is input
	private void turn(Player player) {
		Scanner scan = new Scanner(System.in);
		view.printPlayerTurn(player.getName(), player.getLocation().getName());
		boolean endTurn = false;
		
		while (!endTurn) {
			String[] turnOptions = getTurnOptions();
			if (Manager.isSet(player.getLocation())) {
				Set set = (Set) player.getLocation();
				if (set.getActive() == false) {
					turnOptions[1] = null;
				}
			}
			view.printTurnOptions(turnOptions);
			String choice = scan.nextLine().toLowerCase();
			endTurn = doPlayerChoice(scan, choice);
		}
	}
	
	// Determines what the player would like to do on their turn and 
	// ensures that they are allowed to do that action.
	private boolean doPlayerChoice(Scanner scan, String choice) {
		boolean endTurn = false;
		switch (choice) {
			case "move":
				if (!getHasMoved() && !getHasRole() && player.getRole() == null) {
					move(scan);
					setHasMoved(true);
				}
				break;
			case "take role":
				if (!getHasRole()) {
					takeRole(scan);
					setHasRole(true);
					if (player.getRole() != null) {
						setHasWorked(false);
					}
				}
				break;
			case "work":
				if (!getHasWorked() && getHasRole()) {
					work(scan);
					setHasWorked(true);
				}
				break;
			case "upgrade": 
				if (!getHasUpgraded()) {
					upgrade(scan);
					scan.nextLine();
				}
				break;
			case "end turn":
				endTurn = true;
				break;
		}
		return endTurn;
	}

	// Determines what a player is allowed to do on their turn.
	// Updates as they progress through their turn.
	private String[] getTurnOptions() {
		String[] options = new String[4];
		if (!getHasMoved())
			options[0] = "Move";
		else
			options[0] = null;
		if (!getHasRole() && Manager.isSet(player.getLocation()))
			options[1] = "Take Role";
		else
			options[1] = null;
		if (!getHasWorked() && getHasRole())
			options[2] = "Work";
		else
			options[2] = null;
		if (!getHasUpgraded() && Manager.isOffice(player.getLocation()))
			options[3] = "Upgrade";
		else
			options[3] = null;
		return options;
	}

	//Enables a player to move from room to room across the board.
	private void move(Scanner scan) {
		String[] neighbors = player.getLocation().getNeighbors();
		view.printMoveOptions(neighbors);
		String goTo = scan.nextLine().toLowerCase();

		Board board = Manager.getBoard();
		Room goToLocation = null; //board.getRoomByName(goTo.toLowerCase());
		for (String i : neighbors) {
			if (i.toLowerCase().equals(goTo))
				goToLocation = board.getRoomByName(goTo.toLowerCase());
		}

		while(goToLocation == null){
			view.printMoveOptions(neighbors);
			goTo = scan.nextLine().toLowerCase();
			for (String i : neighbors) {
				if (i.toLowerCase().equals(goTo))
					goToLocation = board.getRoomByName(goTo.toLowerCase());
			}
			goToLocation = board.getRoomByName(goTo.toLowerCase());
		}
		player.updateLocation(goToLocation);
		goToLocation.updatePlayers(player);
	}

	//Enables a player to take a role. Only presents the player with roles
	//that aren't already taken.
	private void takeRole(Scanner scan) {
		Set set = (Set)player.getLocation();
		
		ArrayList<Role> openOnCard = set.getOpenRoles(set.getCard().getParts(), set.getPlayers());
		ArrayList<Role> openOffCard = set.getOpenRoles(set.getParts(), set.getPlayers());		
	
		Role role = null;
		while (role == null) {
			view.printPlayerLevel(player.getRank());
			view.printRoles("On-Card", namesAvailRoles(openOnCard), ranksAvailRoles(openOnCard));
			view.printRoles("Off-Card", namesAvailRoles(openOffCard), ranksAvailRoles(openOffCard));
			view.printBackOption();
			view.printBudget(set.getCard().getBudget());

			String name = scan.nextLine().toLowerCase();
			if (name.equals("no role")) {
				return;
			}
			ArrayList<Role> allRoles = new ArrayList<Role>();
		       	allRoles.addAll(openOnCard);
			allRoles.addAll(openOffCard);
			role = set.getRoleFromList(name, allRoles);	
			if (role == null) {
				view.printBadInput();
			}	
		}
		if (set.getActive() && role.getLevel() <= player.getRank()) {
			player.updateRole(role);
			view.printPlayerRole(player.getName(), role.getPartName());
		}
	}

	//Retrieves the names of a list of Roles
	private String[] namesAvailRoles(ArrayList<Role> parts) {
		String[] names = new String[parts.size()];
		for (int i=0;i<parts.size();i++) {
			names[i] = parts.get(i).getPartName();
		}
		return names;
	}

	//Retrieves the ranks of a list of Roles
	private int[] ranksAvailRoles(ArrayList<Role> parts) {
		int[] ranks = new int[parts.size()];
		for (int i=0;i<parts.size();i++) {
			ranks[i] = parts.get(i).getLevel();
		}
		return ranks;
	}

	//Gives the player the option to act or rehearse for their role
	private void work(Scanner scan) {
		boolean validInput = false;
		Set set = (Set) player.getLocation();
		while (!validInput) {
			view.printNumTakes(set.getNumTakes());
			view.printActOrRehearse();
			String choice = (scan.nextLine().toLowerCase());
			switch (choice) {
				case "act":
					act();
					validInput = true;
					break;
				case "rehearse":
					rehearse();
					validInput = true;
					break;
				default:
					view.printBadInput();
			}
		}
	}

	//Simulates a player acting out their role
	private void act() {
		Set set = (Set) player.getLocation();
		int budget = set.getCard().getBudget();
		boolean onCard = set.getCard().isOnCardRole(player.getRole());

		int attempt = roll() + player.getPracticeChip();
		view.printActing(attempt, budget);
		if (budget <= attempt) {
			view.printLines(player.getRole().getLine());
			view.printSuccessfulActing();
			set.successfulActing();
			paySuccessfulAct(onCard);
		
			if (set.getNumTakes() == 0) {
				payouts(set);
				cleanPlayerRoles(set);
				set.wrapScene();
				view.printWrapScene(set.getName());
			}
		}
		else if (!onCard) {
			player.earnedDollars(1);
			view.printUnsuccessfulActing();
			view.printMadeMoney(player.getName(),"Dollars", 1);
		}
		else {
			view.printUnsuccessfulActing();
		}
	}

	//simulates a player rehearsing for their role
	private void rehearse() {
		player.addPracticeChip();
		view.printLines(player.getRole().getLine());
		view.printRehearse();
	}

	//Pays a player for successful acting
	private void paySuccessfulAct(boolean onCard) {
		if (onCard) {
			player.earnedCredits(2);
			view.printMadeMoney(player.getName(),"Credits", 2);
		}
		else {
			player.earnedCredits(1);
			player.earnedDollars(1);
			view.printMadeMoney(player.getName(),"Credits", 1);
			view.printMadeMoney(player.getName(),"Dollars", 1);
		}
	}

	//When a set closes, the players get paid according to their role and 
	//whether it was on the card or an extra role.
	private void payouts(Set set) {
		boolean anyOnCard = false;
		ArrayList<Player> onCardPlayers = new ArrayList<Player>();
		ArrayList<Player> offCardPlayers = new ArrayList<Player>();
		for (Player i : set.getPlayers()) {
			if (set.getCard().isOnCardRole(i.getRole())) {
				anyOnCard = true;
				onCardPlayers.add(i);
			}
			else  if (i.getRole() == null) {}
			else{
				offCardPlayers.add(i);
			}
		}
		
		if (anyOnCard) {
			payoutOnCardRoles(onCardPlayers, set);
			payoutOffCardRoles(offCardPlayers);
		}
	}

	//Conducts the payouts for Extras
	private void payoutOffCardRoles(ArrayList<Player> offCardPlayers) {
		for (Player pl : offCardPlayers) {
			pl.earnedDollars(pl.getRole().getLevel());
			view.printMadeMoney(pl.getName(),"Dollars", pl.getRole().getLevel());
		}
	}

	//conducts the payouts for On card roles.
	private void payoutOnCardRoles(ArrayList<Player> players, Set set) {
		int[] dice = new int[set.getCard().getBudget()];
		for (int j=0;j<dice.length;j++) {
			dice[j] = roll();
		}
		Arrays.sort(dice);

		//reverses array to be from largest to smallest
		int[] temp = new int[dice.length];
		int j = dice.length;
		for (int i = 0; i < dice.length; i++) {
		    temp[j - 1] = dice[i];
		    j = j - 1;
		}
		dice = temp;

		int numRoles = set.getCard().getParts().size();
		int[] pay = new int[numRoles];
		Arrays.fill(pay, 0);
		pay = calcPayFromDice(pay, dice);
		
		payPlayersByCardRole(players, set.getCard().getParts(), pay);
			
	}

	//for on card roles, this method determines how much the players get paid
	private int[] calcPayFromDice(int[] pay, int[] dice) {
		int payCounter = 0;
		for(int i=0; i<dice.length; i++){
			if(payCounter == pay.length) payCounter=0;
			pay[payCounter] = pay[payCounter]+dice[i];
		}

		return pay;
	}

	private void payPlayersByCardRole(ArrayList<Player> players, ArrayList<Role> parts, int[] pay) {
		for (Player pl : players) {
			for (int i=0; i < parts.size();i++) {
				String name = parts.get(i).getPartName();
				if (name.equals(pl.getRole().getPartName())) {
					pl.earnedDollars(pay[i]);
					view.printMadeMoney(pl.getName(),"Dollars", pay[i]);
				}
			}
		}
	}

	private void cleanPlayerRoles(Set set) {
		for (Player pl : set.getPlayers()) {
			pl.endScene();
		}
	}

	private void upgrade(Scanner scan) {
		int level = 0;
		int[] cost = new int[2];
		boolean validRequest = false;
		while (!validRequest) {
			view.printPlayerLevel(player.getRank());
			view.printPlayerFunds(player.getDollars(), player.getCredits());
			view.printAskForUpgrade();
			
			level = scan.nextInt();
			scan.nextLine();
			cost = upgradeCost(level);
			if (level<7 && level >1) {
				validRequest = true;
			}	
		}
		view.printCost(cost[0], cost[1]);
		String currency = scan.next().toLowerCase();

		//clearing buffer
		scan.nextLine();
		if (player.requestUpgrade(currency, level))
			view.printPlayerLevel(level);
	}

	private int[] upgradeCost(int level) {
		int[] cost = new int[2];
		Board board = Manager.getBoard();
		Upgrade office = (Upgrade) board.getUpgrade();

		cost[0] = office.getLevelAmount(level, "dollar");
		cost[1] = office.getLevelAmount(level, "credit");

		return cost;
	}

	public int roll() {
		Random rand = new Random();

		return rand.nextInt(5) + 1;
	}
}

// Model
//NO TOUCH
//Parsing all the XML files
class ParseXML {
	
	//Singleton Design pattern to ensure no duplicates of
	//the board or cards are made.
	private static ParseXML instance = new ParseXML();

	private ParseXML() {}

	public static ParseXML getInstance() {
		return instance;
	}
	
	//Parses Board.XML.
	//Assumes the file is located in a subdirectory labled "XML"
	public Board parseBoard() throws Exception {
		DocumentBuilderFactory myDomFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder myBuilder = myDomFactory.newDocumentBuilder();
		Document myDoc = myBuilder.parse("./XML/board.xml");

		String name = "Default";

		ArrayList<Room> layout = new ArrayList<Room>();
		
		NodeList setList = myDoc.getElementsByTagName("set");
		layout.addAll(parseSets(setList));

		NodeList trailerList = myDoc.getElementsByTagName("trailer");
		layout.add(parseTrailer(trailerList));
		
		
		NodeList officeList = myDoc.getElementsByTagName("office");
		layout.add(parseOffice(officeList));

		Board board = new Board(name, layout);
		return board;
	}

	private ArrayList<Room> parseSets(NodeList nodes) {
		ArrayList<Room> setList = new ArrayList<Room>();
		
		for (int i=0;i<nodes.getLength();i++) {
			Node child = nodes.item(i);
			String nodeName = child.getNodeName();
			
			setList.add(parseSet(child));
		}

		return setList;
	}

	private Set parseSet(Node sub) {
		String[] neighbors = new String[3];
		int[] area = new int[4];
		int[][] takes = new int[3][4];
		ArrayList<Role> parts = new ArrayList<Role>();

		Element setElement = (Element) sub;
		String name = setElement.getAttribute("name");
		NodeList children = sub.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node subNode = children.item(i);
			String nodeName = subNode.getNodeName();

			switch (nodeName) {
				case "neighbors":
					neighbors = parseNeighbors(subNode);
					break;
				case "area":
					area = parseArea(subNode);
					break;
				case "takes":
					takes = parseTakes(subNode);
					break;
				case "parts":
					parts = parseRoles(subNode);
					break;
			}
		}
		Set set = new Set(name, neighbors, area, takes, parts);
		return set;
	}

	private Trailer parseTrailer(NodeList trailerList) {
		Node node = trailerList.item(0);
		NodeList children = node.getChildNodes();

		String[] neighbors = new String[3];
		int[] area = new int[4];

		for (int i = 0; i < children.getLength(); i++) {
			Node sub = children.item(i);
			String nodeName = sub.getNodeName();
			switch (nodeName) {
				case "neighbors":
					neighbors = parseNeighbors(children.item(i));
					break;
				case "area":
					area = parseArea(children.item(i));
					break;
			}
		}
		Trailer trailer = new Trailer("Trailer", neighbors, area);
		return trailer;
	}

	private Upgrade parseOffice(NodeList officeList) {
		Node node = officeList.item(0);
		NodeList children = node.getChildNodes();
		
		String[] neighbors = new String[3];
		int[] area = new int[4];
		ArrayList<Level> levels = new ArrayList<Level>();
		
		for (int i = 0; i < children.getLength(); i++) {
			Node sub = children.item(i);
			String nodeName = sub.getNodeName();
			switch (nodeName) {
				case "neighbors":
					neighbors = parseNeighbors(children.item(i));
					break;
				case "area":
					area = parseArea(children.item(i));
					break;
				case "upgrades":
					levels = parseUpgrades(children.item(i));
					break;
			}
		}
		Upgrade office = new Upgrade("Office", neighbors, area, levels);
		return office;

	}

	private ArrayList<Level> parseUpgrades(Node node) {
		NodeList children = node.getChildNodes();
		ArrayList<Level> upgrades = new ArrayList<Level>();

		for (int i = 0; i < children.getLength(); i++) {
			Node sub = children.item(i);
			String nodeName = sub.getNodeName();
			if (nodeName.equals("upgrade")) {
				upgrades.add(parseUpgrade(sub));
			}
		}

		return upgrades;
	}

	private Level parseUpgrade(Node node) {
		Element upgrElement = (Element) node;
		int level = Integer.parseInt(upgrElement.getAttribute("level"));
		String currency = upgrElement.getAttribute("currency");
		int amt = Integer.parseInt(upgrElement.getAttribute("amt"));
		
		NodeList children = node.getChildNodes();
		int[] area = new int[4];
		for (int i=0;i<children.getLength();i++) {
			Node sub = children.item(i);
			String nodeName = sub.getNodeName();
			if (nodeName.equals("area")) {
				area = parseArea(sub);
			}
		}	
		Level upgrade = new Level(level, currency, amt, area);
		return upgrade;
	}

	private String[] parseNeighbors(Node sub) {
		NodeList children = sub.getChildNodes();
		int size = children.getLength();

		String[] neighbors = new String[size/2];
		
		for (int i=0;i<size;i++) {
			Node subNode = children.item(i);
			
			String nodeName = subNode.getNodeName();
			if (nodeName.equals("neighbor")) {
				Element neighborName = (Element) subNode;
				neighbors[i/2] = neighborName.getAttribute("name");
			}
		}
		return neighbors;
	}

	private int[][] parseTakes(Node sub) {
		NodeList children = sub.getChildNodes();
		int[][] takes = new int[children.getLength()/2][4];
		
		for (int i = 0; i < children.getLength(); i++) {
			Node subNode = children.item(i);
			String nodeName = subNode.getNodeName();
			
			if (nodeName.equals("take")) {
				int[] area = parseTake(subNode);
				takes[i/2][0] = area[0];
				takes[i/2][1] = area[1];
				takes[i/2][2] = area[2];
				takes[i/2][3] = area[3];
			}
		}
		return takes;
	}

	private int[] parseTake(Node node) {
		Node sub = node.getChildNodes().item(0);
		return parseArea(sub);
	}

	
	//Parses Cards.XML
	//Assumes the file is located in a subdirectory labled "XML"
	public Deck parseCards() throws Exception {
		DocumentBuilderFactory myDomFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder myBuilder = myDomFactory.newDocumentBuilder();
		Document myDoc = myBuilder.parse("./XML/cards.xml");

		NodeList cardList = myDoc.getElementsByTagName("card");

		ArrayList<Card> cards = new ArrayList<Card>();

		for (int i = 0; i < cardList.getLength(); i++) {
			Card card = parseCard(cardList.item(i));
			cards.add(card);
		}
		Deck deck = new Deck(cards);
		return deck;
	}

	private Card parseCard(Node cardNode) {
		Element cardElement = (Element) cardNode;

		String name = cardElement.getAttribute("card name");
		String img = cardElement.getAttribute("img");
		String budget = cardElement.getAttribute("budget");

		String id = null;
		String sceneDesc = null;
		ArrayList<Role> parts = new ArrayList<Role>();

		NodeList child = cardNode.getChildNodes();
		for (int j = 0; j < child.getLength(); j++) {
			Node sub = child.item(j);
			String nodeName = sub.getNodeName();

			switch (nodeName) {
				case "scene":
					Element cardScene = (Element) sub;
					id = cardScene.getAttribute("number");
					sceneDesc = sub.getTextContent();
					break;
				case "part":
					parts.add(parseRole(sub));
					break;
			}
		}
		Card card = new Card(name, Integer.parseInt(budget), Integer.parseInt(id), parts);
		return card;
	}

	private ArrayList<Role> parseRoles(Node node) {
		ArrayList<Role> parts = new ArrayList<Role>();

		NodeList children = node.getChildNodes();
		for (int i=0;i<children.getLength();i++) {
			Node sub = children.item(i);
			String nodeName = sub.getNodeName();

			if (nodeName.equals("part")) {
				parts.add(parseRole(sub));
			}
		}
		return parts;
	}

	private Role parseRole(Node node) {
		String name = null;
		int level = -1;
		int[] area = new int[4];
		String lines = null;

		Element cardPart = (Element) node;
		name = cardPart.getAttribute("name");
		level = Integer.parseInt(cardPart.getAttribute("level"));

		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node sub = children.item(i);
			String nodeName = sub.getNodeName();

			switch (nodeName) {
				case "area":
					area = parseArea(sub);
					break;

				case "line":
					lines = sub.getTextContent();
					break;
			}
		}
		Role part = new Role(name, level, area, lines);
		return part;
	}

	private int[] parseArea(Node sub) {
		int[] area = new int[4];
		Element location = (Element) sub;

		area[0] = Integer.parseInt(location.getAttribute("x"));
		area[1] = Integer.parseInt(location.getAttribute("y"));
		area[2] = Integer.parseInt(location.getAttribute("h"));
		area[3] = Integer.parseInt(location.getAttribute("w"));

		return area;
	}
}

class UserInterface {
	
	public UserInterface() {}

	public void printGetNumPlayers() {
		System.out.println("How many players? (2-8)");
	}

	public void printGetPlayer(int playerNum) {
		System.out.printf("What is player %d's name?\n", playerNum);
	}

	public void printPlayerName(String name) {
		System.out.println("Player: "+name);
	}
	
	public void printPlayerLocation(String room) {
		System.out.printf("Player in room %s\n", room);
	}

	public void printPlayerRole(String name, String role) {
		System.out.println();
		System.out.printf("%s's role is %s.\n",	name, role);
	}

	public void printPlayerLevel(int level) {
		System.out.printf("\nPlayer level is %d.\n", level);
	}

	public void printPlayerTurn(String name, String location) {
		System.out.println();
		System.out.printf("It's now %s's turn. They are in %s\n", name, location);
	}

	public void printBadInput() {
		System.out.println();
		System.out.println("Bad input.");
	}

	public void printTurnOptions(String[] options) {
		System.out.printf("Pick: ");
		for (String i : options) {
			if (i != null) 
				System.out.printf(" %s      ", i);
		}
		System.out.println("End turn");
	}
	
	public void printMoveOptions(String[] options) {
		System.out.println();
		System.out.printf("Pick: ");
		for (int i=0;i<options.length;i++) {
			System.out.printf("%s     ", options[i]);
		}
			System.out.println();
	}

	public void printBudget(int budget) {
		System.out.printf("The budget of this set is %d\n\n", budget);
	}

	public void printRoles(String type, String[] names, int[] ranks) {
		System.out.printf("%s Roles:\n", type);
		for (int i=0;i<names.length;i++) {
			System.out.printf("%s (Rank %d)     ", names[i], ranks[i] );
		}
		System.out.println();
	}
	
	public void printActOrRehearse() {
		System.out.println();
		System.out.println("Would you like to 'act' or 'rehearse' for your role?");
	}
	
	public void printSuccessfulActing() {
		System.out.println("Successful acting!");
	}
	
	public void printUnsuccessfulActing() {
		System.out.println("Better luck next time.");
	}
	
	public void printMadeMoney(String name, String moneyType, int amt) {
		System.out.printf("%s made %d %s!\n",name, amt, moneyType);
	}
	
	public void printRehearse() {
		System.out.println("You gained one practice chip.");
		System.out.println();
	}
	
	public void printPlayerFunds(int dollars, int credits) {
		System.out.printf("Player has %d dollars and %d credits\n", dollars, credits);
	}
	
	public void printAskForUpgrade() {
		System.out.println();
		System.out.println("What level would you like to upgrade to (max = 6)?");
	}
	
	public void printDollarsOrCredits() {
		System.out.println();
		System.out.println("Would you like to use dollars or credits?");
	}
	
	public void printCost(int dollars, int credits) {
		System.out.println();
		System.out.printf("It will cost %d dollars or %d credits. How would you like to pay?\n Pick: 'dollar', 'credit', or 'cancel'\n", dollars, credits);
	}	
	
	public void printBackOption() {
		System.out.println("Or enter 'no role' if you don't want a role or can't take one.");
	}

	public void printActing(int attempt, int budget) {
		System.out.printf("You rolled %d, and needed at least %d\n", attempt, budget);
	}

	public void printNumTakes(int takes) {
		System.out.printf("\nShot counters left for this set: %d", takes);
	}

	public void printLines(String lines) {
		System.out.println(lines);
	}

	public void printWrapScene(String set) {
		System.out.printf("%s is finished shooting! All roles are now closed here.\n\n", set);
	}
}
