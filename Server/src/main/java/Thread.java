package com.lab5;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    static ResourceBuffer CR1 = new ResourceBuffer();
    static ResourceVariables CR2 = new ResourceVariables();
    static Semaphore sem1 = new Semaphore(0, true); // for P1
    static Semaphore sem2 = new Semaphore(0, true); // for P4
    static CyclicBarrier br1 = new CyclicBarrier(2); // for P1 and P2
    static CyclicBarrier br2 = new CyclicBarrier(2); // for P2 and P4


    public static void main(String[] args) {
        Thread1 thread1 = new Thread1();
        Thread2 thread2 = new Thread2();
        Thread3 thread3 = new Thread3();
        Thread4 thread4 = new Thread4();
        Thread5 thread5 = new Thread5();
        Thread6 thread6 = new Thread6();

        try {
            thread1.t.join();
            thread2.t.join();
            thread3.t.join();
            thread4.t.join();
            thread5.t.join();
            thread6.t.join();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}

class ResourceBuffer {
    private static final int MaxBufSize = 24;
    private static final int MinBufSize = 0;
    int buff[] = new int[MaxBufSize+1];
    int ind = 0;
    boolean IsEmpty = ind  == MinBufSize;
    boolean IsFull = ind == MaxBufSize;
    int counter = 100;

    synchronized void consume(String str) {
        while (IsEmpty)
            try {
                wait();
            }
            catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }

        System.out.println(str + ": --- buf[" + ind + "] = " + buff[ind]);
        buff[ind] = 0;
        System.out.println(str + ":--- ind: " + ind-- + " => " + ind);

        IsEmpty = ind == MinBufSize;
        IsFull = false;

        notify();
    }


    synchronized void produce (String str) {
        while (IsFull) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }
        }

        System.out.println(str + ": +++ ind: " + ind++ +" => " + ind);
        buff[ind] = ind;
        System.out.println(str + ": +++ buf[" + ind + "] = " + buff[ind]);
        counter--;

        IsFull = ind == MaxBufSize;
        IsEmpty = false;

        notify();

        if (counter == 0) System.exit(0);
    }
}

class ResourceVariables {

    private static Random random = new Random();

    private byte aByte = 0;
    private short aShort = 0;
    private int anInt= 0;
    private long aLong = 0;
    private char aChar = '0';
    private boolean aBoolean = false;
    private double aDouble= 0;
    private float aFloat = 0;

    public void consume(String str) {
        System.out.println(str + ": CR2 variables consume: " + aShort + " " + aChar + " " + aDouble + " " + aFloat + " " + aLong);
    }

    public void produce(String str) {
        aByte = 1;
        anInt= random.nextInt();
        aLong = random.nextLong();
        aChar = 'd';
        aBoolean = random.nextBoolean();
        aDouble= random.nextDouble();
        aFloat = random.nextFloat();
        System.out.println(str + ": CR2 variables produce: " + aShort + " " + aChar + " " + aDouble + " " + aFloat + " " + aLong);
    }
}

class Thread1 implements Runnable {

    public Thread t;
    private Lock mutex = new ReentrantLock();

    public Thread1() {
        t = new Thread(this,"P1");
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P1 started!");
        while (true) {
            mutex.lock();
            System.out.println("P1: Mutex lock");
            Main.CR2.produce(t.getName());
            System.out.println("P1: Mutex unlock");
            mutex.unlock();

            System.out.println("P1: Semaphore before sync");
            Main.sem2.release();
            try {
                Main.sem1.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("P1: Semaphore after sync");

            mutex.lock();
            System.out.println("P1: Mutex lock");
            Main.CR2.consume(t.getName());
            System.out.println("P1: Mutex unlock");
            mutex.unlock();

            System.out.println("P1: Barrier (with P2) wait");
            try {
                Main.br1.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println("P1: Barrier (with P2) sync");
        }
    }
}

class Thread2 implements Runnable {

    public Thread t;

    public Thread2() {
        t = new Thread(this,"P2");
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P2 started!");
        while (true) {
            System.out.println("P2: Barrier (with P5) wait");
            try {
                Main.br2.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println("P2: Barrier (with P5) sync");


            System.out.println("P2: Barrier (with P1) wait");
            try {
                Main.br1.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println("P2: Barrier (with P1) sync");

            Main.CR1.consume(t.getName());
        }
    }
}

class Thread3 implements Runnable {

    public Thread t;

    public Thread3() {
        t = new Thread(this,"P3");
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P3 started!");
        while (true) {
            Main.CR1.consume(t.getName());
//            Thread.yield();
        }
    }
}

class Thread4 implements Runnable {

    public Thread t;
    private Lock mutex = new ReentrantLock();

    public Thread4() {
        t = new Thread(this,"P4");
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P4 started!");
        while (true) {
            System.out.println("P4: Semaphore before sync");
            Main.sem1.release();
            try {
                Main.sem2.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("P4: Semaphore after sync");

            mutex.lock();
            System.out.println("P4: Mutex lock");
            Main.CR2.consume(t.getName());
            System.out.println("P4: Mutex unlock");
            mutex.unlock();
        }
    }
}

class Thread5 implements Runnable {

    public Thread t;

    public Thread5() {
        t = new Thread(this,"P5");
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P5 started!");
        while (true) {
            System.out.println("P5: Barrier (with P2) wait");
            try {
                Main.br2.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println("P5: Barrier (with P2) sync");

            Main.CR1.produce(t.getName());
        }
    }
}

class Thread6 implements Runnable {

    public Thread t;

    public Thread6() {
        t = new Thread(this,"P6");
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P6 started!");
        while (true) {
            Main.CR1.produce(t.getName());
            Thread.yield();
        }
    }
}
