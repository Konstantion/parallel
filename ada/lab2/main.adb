with Ada.Text_IO;         use Ada.Text_IO;
with Ada.Integer_Text_IO; use Ada.Integer_Text_IO;
with Ada.Real_Time;       use Ada.Real_Time;
with Ada.Numerics.Discrete_Random;

procedure Main is
    Dim        : constant Integer := 200_000;
    Thread_Num : constant Integer := 2;

    type Int_Array is array (1 .. Dim) of Integer;
    Arr : Int_Array;
    subtype Random_Range is Integer range 1 .. Dim;

    package Random_Gen is new Ada.Numerics.Discrete_Random (Random_Range);
    use Random_Gen;

    procedure Init_Arr is
        Gen : Generator;
    begin
        Reset (Gen);
        for I in Arr'Range loop
            Arr (I) := Random (Gen);
        end loop;
        Arr (Random (Gen)) := -1;
    end Init_Arr;

    function Part_Min (Start_Index, Finish_Index : Integer) return Integer is
        Min_Value : Integer := Integer'Last;
        Min_Index : Integer := 0;
    begin
        for I in Start_Index .. Finish_Index loop
            if Arr (I) < Min_Value then
                Min_Value := Arr (I);
                Min_Index := I;
            end if;
        end loop;
        return Min_Index;
    end Part_Min;

    task type Worker is
        entry Start (Start_Index, Finish_Index : Integer);
    end Worker;

    protected type Part_Manager_Type is
        procedure Set_Part_Min (Min_Index : Integer);
        entry Get_Min (Min_Index : out Integer);
    private
        Current_Min     : Integer := Integer'Last;
        Min_Index_Local : Integer := 0;
        Task_Count      : Integer := 0;
    end Part_Manager_Type;

    protected body Part_Manager_Type is
        procedure Set_Part_Min (Min_Index : Integer) is
        begin
            if Arr (Min_Index) < Current_Min then
                Current_Min     := Arr (Min_Index);
                Min_Index_Local := Min_Index;
            end if;
            Task_Count := Task_Count + 1;
        end Set_Part_Min;

        entry Get_Min (Min_Index : out Integer) when Task_Count = Thread_Num is
        begin
            Min_Index := Min_Index_Local;
        end Get_Min;
    end Part_Manager_Type;

    Part_Manager : Part_Manager_Type;

    task body Worker is
        Local_Start, Local_Finish : Integer;
    begin
        accept Start (Start_Index, Finish_Index : Integer) do
            Local_Start  := Start_Index;
            Local_Finish := Finish_Index;
        end Start;

        Part_Manager.Set_Part_Min (Part_Min (Local_Start, Local_Finish));
    end Worker;

    function Parallel_Find_Min return Integer is
        Workers    : array (1 .. Thread_Num) of Worker;
        Range_Size : Integer := Dim / Thread_Num;
        Min_Index  : Integer;
    begin
        for I in Workers'Range loop
            Workers (I).Start ((I - 1) * Range_Size + 1, I * Range_Size);
        end loop;

        Part_Manager.Get_Min (Min_Index);
        return Min_Index;
    end Parallel_Find_Min;

    Min_Index    : Integer;
    Start_Time   : Time;
    Stop_Time    : Time;
    Elapsed_Time : Time_Span;
begin
    Start_Time := Clock;
    Init_Arr;
    Min_Index    := Parallel_Find_Min;
    Stop_Time    := Clock;
    Elapsed_Time := Stop_Time - Start_Time;

    Put_Line ("Minimum value in array: " & Integer'Image (Arr (Min_Index)));
    Put_Line ("Index of minimum value: " & Integer'Image (Min_Index));
    Put_Line
       ("Elapsed time: " & Duration'Image (To_Duration (Elapsed_Time)) &
        " seconds");
end Main;
