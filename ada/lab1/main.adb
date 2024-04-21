with Ada.Text_IO;

procedure Main is

    Can_Stop : Boolean := False;
    pragma Volatile (Can_Stop);

    Thread_Amount : Natural := 2;

    task type Notifier_Thread;
    task type Computer_Thread is
        entry Start (Id : Integer; Step : Integer);
    end Computer_Thread;

    task body Notifier_Thread is
    begin
        delay 4.0;
        Can_Stop := True;
    end Notifier_Thread;

    task body Computer_Thread is
        Id   : Integer;
        Step : Integer;
        Sum  : Long_Long_Integer := 0;
    begin
        accept Start (Id : Integer; Step : Integer) do
            Computer_Thread.Id   := Id;
            Computer_Thread.Step := Step;
        end Start;
        while not Can_Stop loop
            delay 1.0;
            Sum := Sum + Long_Long_Integer (Step);
            exit when Can_Stop;
        end loop;

        Ada.Text_IO.Put_Line
           ("Thread " & Integer'Image (Id) & " " & "Sum: " &
            Long_Long_Integer'Image (Sum));
    end Computer_Thread;

    Notifier  : Notifier_Thread;
    Computers : array (1 .. Thread_Amount) of Computer_Thread;
begin
    for I in 1 .. Thread_Amount loop
        Computers (I).Start (Id => I, Step => I + 1);
    end loop;
end Main;
