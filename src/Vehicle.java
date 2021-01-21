import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Vehicle extends JFrame{
    private Socket connection;

    private JButton hostButton;
    private JButton connectButton;
    private JTextField IPInput;
    private JComboBox selectMouse;
    private JPanel vehicleForm;
    private JLabel vehicleLabel;
    private JLabel statusLbl;

    private String selectedVehicle;

    public static void main(String[] args) {

        Vehicle vehicle = new Vehicle();
        vehicle.setVisible(true);
    }

    public Vehicle() {
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToHost();
            }
        });
        hostButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostANetwork();
            }
        });
        vehicleForm.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                moveVehicle(e.getX(),e.getY());
            }
        });
        add(vehicleForm);
        setTitle("Peer to Peer Game");
        setSize(800,800);
        vehicleForm.setBounds(100,100,800,800);

        vehicleLabel.setIcon(new ImageIcon("img/motorbike.png"  ));
        selectMouse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeVehicle();
            }
        });
    }


    private void changeVehicle() {
        String vehicle = selectMouse.getSelectedItem().toString();
        if(vehicle.equals("Car")) {
            vehicleLabel.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/car.png"));
            selectedVehicle = "Car";
        } else {
            vehicleLabel.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/motorbike.png"));
            selectedVehicle = "Motor";
        }
    }

    private void moveVehicle(int x, int y) {
        vehicleLabel.setBounds(x, y, vehicleLabel.getWidth(), vehicleLabel.getHeight());
        if(connection != null) {
            Thread thread = new Vehicle.MovementUpdateThread(x, y, selectedVehicle);
            thread.start();
        }
    }


    private void hostANetwork() {
        Thread thread = new Vehicle.NetworkHostingThread();
        thread.start();
        statusLbl.setText("Hosting...");
    }

    private void connectToHost() {
        Thread thread = new Vehicle.ConnectionThread();
        thread.start();

    }

    private class NetworkHostingThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                ServerSocket serverSocket = new ServerSocket(9999);
                // set label hosting
                while(true) {
                    connection = serverSocket.accept();
                    Thread movementThread = new Vehicle.MovementReaderThread();
                    movementThread.start();
                    statusLbl.setText("Connected");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            super.run();

//			String hostAddress = txtAddress.getText();
            String hostAddress = "localhost";
            try {
                connection = new Socket(hostAddress, 9999);
                Thread movementThread = new Vehicle.MovementReaderThread();
                movementThread.start();
                statusLbl.setText("Connected");
            } catch (IOException e) {
                System.out.println("Connection fail. " + e.getMessage());
                statusLbl.setText("Connection failed");
            }
        }
    }

    private class MovementReaderThread extends Thread {
        @Override
        public void run() {
            super.run();

            try {
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                while(true) {
                    try {
                        String coordinate = scanner.nextLine();
                        String[] point = coordinate.split("#");
                        vehicleLabel.setBounds(Integer.parseInt(point[0]), Integer.parseInt(point[1]), vehicleLabel.getWidth(), vehicleLabel.getHeight());
                        if(point[2].equals("Car")) {
                            vehicleLabel.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/car.png"));
                            selectMouse.setSelectedIndex(1);
                        }else {
                            vehicleLabel.setIcon(new ImageIcon(System.getProperty("user.dir") + "/img/motorbike.png"));
                            selectMouse.setSelectedIndex(0);
                        }
                    }catch(java.util.NoSuchElementException e) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class MovementUpdateThread extends Thread {
        private int x;
        private int y;
        private String vehicle;

        public MovementUpdateThread(int x, int y, String vehicle) {
            super();
            this.x = x;
            this.y = y;
            this.vehicle = vehicle;
        }

        @Override
        public void run() {
            super.run();

            try {
                OutputStream outputStream = connection.getOutputStream();
                PrintWriter printWriter = new PrintWriter(outputStream);
                String message = x + "#" + y + "#" + vehicle;
                printWriter.write(message + "\n");
                printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
