package model;

import org.junit.runner.*;
import org.junit.runners.*;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	ColumnTests.class,
	NodeTests.class,
	MapTests.class,
	PlayerBoardTests.class,
	PlayerTests.class,
	CardsTests.class,
})
public class RunTests {}
