with Ada.Text_IO;

procedure Main is

    Can_Stop : Boolean := False;
    pragma Volatile (Can_Stop);

    Thread_Amount : Natural           := 2;

    task type Break_Thread;
    task type Computer_Thread is
        entry Start (Thread_Id : Integer; Step : Integer);
    end Computer_Thread;

    task body Break_Thread is
    begin
        delay 4.0;
        Can_Stop := True;
    end Break_Thread;

    task body Computer_Thread is
        Id_Private   : Integer;
        Step_Private : Integer;
        Sum  : Long_Long_Integer :=  0;
    begin
        accept Start (Thread_Id : Integer; Step : Integer) do
            Id_Private   := Thread_Id;
            Step_Private := Step;
        end Start;
        while not Can_Stop loop
            delay 1.0;
            Sum := Sum + Long_Long_Integer(Step_Private);
            exit when Can_Stop;
        end loop;

        Ada.Text_IO.Put_Line ("Thread " & Id_Private'Img & " " & "Sum: " & Sum'Img);
    end Computer_Thread;

    Breaker : Break_Thread;
    T  : array (1 .. Thread_Amount) of Computer_Thread;
begin
    for I in 1 .. Thread_Amount loop
        T (I).Start (Thread_Id => I, Step => I + 1);
    end loop;
end Main;
