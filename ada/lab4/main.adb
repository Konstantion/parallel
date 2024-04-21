with Ada.Text_IO;     use Ada.Text_IO;
with GNAT.Semaphores; use GNAT.Semaphores;

procedure Main is
    USE_TOKEN              : constant Boolean := False;
    FORKS_COUNT            : constant Integer := 5;
    AVAILABLE_EATING_SEATS : constant Integer := 2;

    Forks               :
       array (1 .. FORKS_COUNT) of Counting_Semaphore (1, Default_Ceiling);
    Eating_Philosophers :
       Counting_Semaphore (AVAILABLE_EATING_SEATS, Default_Ceiling);

    task type Philosopher is
        entry Start (Id : Integer);
    end Philosopher;

    task body Philosopher is
        Id                    : Integer;
        Left_Fork, Right_Fork : Integer;
    begin
        accept Start (Id : in Integer) do
            Philosopher.Id := Id;
        end Start;

        if not USE_TOKEN then
            if Id = FORKS_COUNT then
                Right_Fork := Id;
                Left_Fork  := Id rem FORKS_COUNT + 1;
            else
                Left_Fork  := Id;
                Right_Fork := Id rem FORKS_COUNT + 1;
            end if;
        else
            Left_Fork  := Id;
            Right_Fork := Id rem FORKS_COUNT + 1;
        end if;

        for I in 1 .. 10 loop
            Put_Line
               ("Philosopher " & Integer'Image (Id) & " is thinking. Round: " &
                Integer'Image (I));
            if USE_TOKEN then
                Eating_Philosophers.Seize;
            end if;

            Forks (Left_Fork).Seize;
            Put_Line
               ("Philosopher " & Integer'Image (Id) & " took left fork #" &
                Integer'Image (Left_Fork));

            Forks (Right_Fork).Seize;
            Put_Line
               ("Philosopher " & Integer'Image (Id) & " took right fork #" &
                Integer'Image (Right_Fork));

            Put_Line
               ("Philosopher " & Integer'Image (Id) & " is eating. Round: " &
                Integer'Image (I));

            Forks (Right_Fork).Release;
            Forks (Left_Fork).Release;
            if USE_TOKEN then
                Eating_Philosophers.Release;
            end if;

            Put_Line
               ("Philosopher " & Integer'Image (Id) &
                " finished eating. Round: " & Integer'Image (I));
        end loop;
    end Philosopher;

    Philosophers : array (1 .. FORKS_COUNT) of Philosopher;
begin
    for I in Philosophers'Range loop
        Philosophers (I).Start (I);
    end loop;
end Main;
