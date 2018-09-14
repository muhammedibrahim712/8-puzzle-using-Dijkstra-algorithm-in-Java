/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 *
 * @author skyst
 */
public class Puzzle {
    
    static class Heap{

        private BoardState[] stateArr;
        private int maxSize; 
        private int curSize; 

        public Heap(int mx){
            maxSize = mx;
            curSize = 0;
            stateArr = new BoardState[maxSize]; 
        }

        public boolean isEmpty(){
            return curSize == 0;
        }

        public boolean insert(BoardState ele){
            if (curSize + 1 == maxSize) return false;

            stateArr[++curSize] = ele;
            int pos = curSize;

            while(pos != 1 && (ele.cost < stateArr[pos/2].cost ||
                               (ele.cost == stateArr[pos/2].cost && ele.path_len < stateArr[pos/2].path_len))){
                stateArr[pos] = stateArr[pos/2];
                pos /= 2;
            }

            stateArr[pos] = ele;
            return true;
        } 

        public BoardState remove(){
            int parent, child;
            BoardState temp;
            BoardState rlt;

            if (isEmpty()) throw new RuntimeException("Error : Heap empty!");

            rlt = stateArr[1];
            temp = stateArr[curSize--];

            parent = 1;
            child = 2;
            while (child <= curSize){
                if (child < curSize && ((stateArr[child].cost > stateArr[child + 1].cost) ||
                                        (stateArr[child].cost == stateArr[child + 1].cost &&
                                         stateArr[child].path_len > stateArr[child + 1].path_len))) child++;
                if (temp.cost < stateArr[child].cost || (temp.cost == stateArr[child].cost &&
                                                         temp.path_len < stateArr[child].path_len)) break;

                stateArr[parent] = stateArr[child];
                parent = child;
                child *= 2;
            }
            stateArr[parent] = temp;

            return rlt;

        } 
    }

    static class BoardState {
        int[][] state = null;
        int cost;
        int path_len;
        int zeroR;
        int zeroC;
        String state_str;
        Queue<String> path;

        public BoardState(){
            state = new int[3][3];
            cost = 0;
            path_len = 0;
            zeroR = -1;
            zeroC = -1;
            state_str = "";
            path = new LinkedList<String>();
        }

        public BoardState(String s, int val, int len){
            int i, j;

            state = new int[3][3];
            state_str = s;

            for(i = 0; i < 3; i++){
                for(j = 0; j < 3; j++){
                    if(s.charAt(i*3+j) == 'G'){
                        state[i][j] = 0;
                        zeroR = i;
                        zeroC = j;
                    }
                    else state[i][j] = (s.charAt(i * 3 + j) - '0');
                }
            }

            cost = val;
            path_len = len;
        }    
    }


    static class Solution {

        private final static int HEAP_MAX_SIZE = 362880;

        int dr[] = {0, -1, 0, 1};
        int dc[] = {-1, 0, 1, 0};
        char dirC[] = {'R', 'D', 'L', 'U'};

        public int cntInversion(int[][] arr){
            int rlt = 0;

            int[] tmp = null;
            tmp = new int[9];

            int i, j;
            for(i = 0; i < 3; i++){
                for(j = 0; j < 3; j++){
                    tmp[i * 3 + j] = arr[i][j];
                }
            }


            for(i = 0; i < 9; i++){
                for(j = i + 1; j < 9; j++){
                    if(tmp[i] > tmp[j]) rlt++;
                }
            }

            return rlt;
        }

        public boolean isGoalState(BoardState cur, BoardState goal){
            if(cur.zeroC != goal.zeroC || cur.zeroR != goal.zeroR) return false;

            for(int i = 0; i < 3; i++){
                for(int j = 0; j < 3; j++){
                    if(cur.state[i][j] != goal.state[i][j]) return false;
                }
            }

            return true;
        }

        public void solvePuzzle(String s_str, String g_str, int[] cost, BufferedWriter output) throws IOException{
            BoardState s_state = new BoardState(s_str, 0, 0);
            BoardState g_state = new BoardState(g_str, 0, 1);
            String out_str = new String("");

            int i = cntInversion(s_state.state);
            int j = cntInversion(g_state.state);

            if((i + j) % 2 == 1){
                out_str = out_str + "-1 -1";
                output.write(out_str + "\n" + "\n");
                return;
            }

            String mp_cnt = "Y";

            HashMap<String, String> myHashMap = new HashMap<String, String>();
            myHashMap.clear();

            Heap myHeap = new Heap(HEAP_MAX_SIZE);                
            myHeap.insert(s_state);

            BoardState temp = new BoardState();

            while(myHeap.isEmpty() == false){            

                temp = myHeap.remove();
                myHashMap.put(temp.state_str, mp_cnt);

                if(isGoalState(temp, g_state) == true) break;            

                int rr = temp.zeroR;
                int cc = temp.zeroC;         

                for(i = 0; i < 4; i++){

                    int r = rr + dr[i];
                    int c = cc + dc[i];

                    if(r < 0 || c < 0 || r >= 3 || c >= 3) continue;

                    int cst = temp.state[r][c];
                    cst = cost[cst - 1];

                    BoardState inTmp = new BoardState();

                    for(int ii = 0; ii < 3; ii++){
                        for(int jj = 0; jj < 3; jj++) inTmp.state[ii][jj] = temp.state[ii][jj];
                    }

                    char[] tPath = null;
                    tPath = new char[2];
                    tPath[0] = (char)(temp.state[r][c] + '0');
                    tPath[1] = dirC[i];


                    inTmp.zeroR = r;
                    inTmp.zeroC = c;
                    inTmp.state[rr][cc] = temp.state[r][c];
                    inTmp.state[r][c] = temp.state[rr][cc];
                    inTmp.cost = temp.cost + cst;
                    inTmp.path_len = temp.path_len + 1;
                    char[] tmp_str = null;
                    tmp_str = new char[9];

                    for(int ii = 0; ii < 3; ii++){
                        for(int jj = 0; jj < 3; jj++){
                            tmp_str[ii*3+jj] = (char)(inTmp.state[ii][jj] + '0');
                        }
                    }

                    inTmp.state_str = String.valueOf(tmp_str);                

                    if(myHashMap.containsKey(inTmp.state_str) == true) continue;
                    if(temp.path_len > 0){
                        Iterator<String> edge = temp.path.iterator();
                        while(edge.hasNext()){
                            inTmp.path.add(edge.next());
                        }
                    }

                    inTmp.path.add(String.valueOf(tPath));
                    myHeap.insert(inTmp);

                }

            }

            out_str = String.valueOf(temp.path_len) + " " + String.valueOf(temp.cost);        
            output.write(out_str + "\n");
            out_str = "";
            if(temp.path_len == 0){            
                output.write(out_str + "\n");
                return;
            }



            int flag = 0;
            while(temp.path.isEmpty() == false){
                if(flag == 1) out_str = out_str + " ";
                flag = 1;
                out_str = out_str + temp.path.remove();
            }
            output.write(out_str + "\n");
        }
    }    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
            
        //System.out.println("Why don't input");
        
        // TODO code application logic here
        String inputFileName = args[0];
        String outputFileName = args[1];
        //System.out.println(inputFileName + "  " + outputFileName);
        try {
         
// Input data from file named "in"
         //File file = new File("input.txt");
         File file = new File(inputFileName);
         Scanner scanner = new Scanner(file);

// Output in file named "out"         
         BufferedWriter output = null;
         //File ofile = new File("output.txt");
         File ofile = new File(outputFileName);
         output = new BufferedWriter(new FileWriter(ofile));
         
         int T = scanner.nextInt();
         for(int t_case = 0; t_case < T; t_case++){
             String start_str = scanner.next();
             String goal_str = scanner.next();
             int[] cost = null;
             cost = new int[8];
             
             for(int i = 0; i < 8; i++){
                 cost[i] = scanner.nextInt();
             }
             
             Solution sol = new Solution();             
             sol.solvePuzzle(start_str, goal_str, cost, output);
             //System.out.println("END");
         }
         
         output.close();
       } catch (FileNotFoundException e) {
         e.printStackTrace();
       }
        
    }
    
}
