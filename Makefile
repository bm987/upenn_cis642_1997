# $Id: Makefile,v 1.1 1997/04/30 14:31:16 berrym Exp berrym $
#
# Makefile for real time simulator
#
# Mike Berry
# CIS 642
# 24 April 1997

all: simApplet
 
simApplet: simApplet.class

simApplet.class: sim.java simApplet.java
	javac simApplet.java sim.java 

	